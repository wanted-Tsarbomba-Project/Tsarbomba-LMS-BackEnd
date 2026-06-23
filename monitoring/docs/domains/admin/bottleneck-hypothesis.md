# Admin 도메인 — 병목 가설 (7단계 §1단계)

> 프로세스: [`../../PROCESS.md`](../../PROCESS.md) · 컨벤션: [`../../CONVENTION.md`](../../CONVENTION.md)
> 이 문서는 부하테스트 전에 "무엇이 왜 느릴 것인가"를 가설로 고정하는 산출물이다. baseline과 전후 비교로 검증한다.

## 도메인 유형 분류

Admin 도메인은 운영자 화면용 조회 API와 운영 자동화 배치를 함께 가진다.

| 트랙 | 대상 | 유형 | 6단계 처리 방식 |
|------|------|------|------------------|
| **A. 목록 조회** | `GET /api/v1/admin/operation-alerts` | 조회/집계형 | 필터 + 페이징 + 정렬 쿼리의 인덱스/정렬 비용 확인 |
| **B. 상세 조회** | `GET /api/v1/admin/operation-alerts/{operationAlertId}` | 단건 조회 + 타 도메인 조합 | alert 상세 조회 후 target type별 추가 조회 비용 확인 |
| **C. 자동화 실행** | `OperationRuleExecutionService.run()` | 조회/집계형 + 배치형 | 학생/강좌/제출 지표 조회, 탐지 결과 upsert 반복 비용 확인 |

---

## 병목 가설표

| # | 대상 | 병목 가설 | 근거 (코드 위치) | 관찰 지표 | 성공 기준 |
|---|------|-----------|------------------|-----------|-----------|
| 1 ★ | `OperationRuleExecutionService.run()` | **USER 규칙 N+1.** 학생 전체 조회 후 학생마다 최신 로그인 이력을 개별 조회한다. 학생 수가 늘면 `1 + N` 형태로 DB 조회가 증가한다. | [`UserOperationMetricAdapter.findInactiveUsers`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/automation/infrastructure/adapter/UserOperationMetricAdapter.java) → `findStudents()` 후 `findLatestLoginAt(student.userId())` 반복 | `admin_operation_rule_run_duration`, `admin_operation_rule_detect_duration{ruleCode=...}`, Hikari active, SQL 로그 수 | 학생 대량 시드 기준 실행시간이 선형 급증하지 않을 것, Hikari 포화 없음 |
| 2 ★ | `OperationRuleExecutionService.run()` | **탐지 결과별 alert upsert 반복 비용.** 탐지 결과마다 open alert 조회 후 save를 수행한다. 결과 수가 많으면 조회/쓰기/락 비용이 커질 수 있다. | [`OperationRuleExecutionService.saveAlert`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/automation/application/service/OperationRuleExecutionService.java) → `findOpenByRuleIdAndTarget()` + `save()` | `admin_operation_alert_upsert_duration`, 탐지 결과 수, Hikari active, transaction duration | 탐지 결과 N건 증가 시 upsert 시간이 과도하게 증가하지 않을 것 |
| 3 | `GET /api/v1/admin/operation-alerts` | **필터 + 정렬 인덱스 부재 가능성.** `deletedAt`, `targetType`, `status` 조건 후 `lastDetectedAt desc`, `operationAlertId desc` 정렬을 수행한다. alert row가 많으면 filesort/scan 비용이 커질 수 있다. | [`SpringDataOperationAlertRepository.findAlerts`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/alert/infrastructure/persistence/SpringDataOperationAlertRepository.java), [`OperationAlertQueryAdapter.findAlerts`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/alert/infrastructure/persistence/OperationAlertQueryAdapter.java) | `http_server_requests_seconds{domain="admin",uri="/api/v1/admin/operation-alerts"}`, `admin_operation_alert_list_query_duration`, EXPLAIN rows/key | VU 30~50, alert 대량 시드 기준 p95 < 500~800ms, 실패율 < 1% |
| 4 | `GET /api/v1/admin/operation-alerts/{operationAlertId}` | **상세 조회 후 target detail 조합 비용.** alert + rule 조회 뒤 target type에 따라 course/problem/user 상세를 추가 조회한다. target 도메인 상태에 따라 tail latency가 튈 수 있다. | [`OperationAlertQueryService.getAlertDetail`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/alert/application/service/OperationAlertQueryService.java), [`OperationAlertTargetDetailAdapter.loadTargetDetail`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/alert/infrastructure/adapter/OperationAlertTargetDetailAdapter.java) | `admin_operation_alert_detail_query_duration`, `admin_operation_alert_target_detail_duration`, Loki `durationMs`, traceId | 단건 상세 조회 p95 < 300~500ms, target type별 outlier 확인 |

★ = admin 도메인 메인 측정 후보.

---

## 트랙별 검증 관점

### A. 운영 알림 목록 조회

- alert row를 충분히 시드한 뒤 필터 조건별 p95를 비교한다.
- `targetType`, `status`가 있을 때와 없을 때를 나눠 EXPLAIN을 확인한다.
- `lastDetectedAt desc`, `operationAlertId desc` 정렬이 인덱스를 타는지 확인한다.

### B. 운영 알림 상세 조회

- `COURSE`, `PROBLEM`, `USER` target type별 상세 조회 시간을 따로 본다.
- 공통 alert 상세 query와 target detail query 시간을 분리한다.
- traceId로 alert 조회 이후 타 도메인 조회가 몇 번 발생하는지 확인한다.

### C. 자동화 실행

- `USER_INACTIVE_NO_COURSE` 계열은 학생 수에 따른 N+1 여부가 핵심이다.
- `COURSE_LOW_ENROLLMENT`는 전체 active enrollment와 전체 course를 메모리에서 조합한다.
- `PROBLEM_HIGH_WRONG_RATE`는 제출 통계 집계 쿼리 비용을 확인한다.
- 탐지 결과가 많을수록 alert upsert가 반복되므로 detect와 save 구간을 분리해 본다.

---

## 다음 단계

- **2단계**: `admin_operation_*` Timer/Counter와 `event=admin_*` 로그 설계 → [`metrics.md`](metrics.md)
- **4·5단계**: 운영 알림/학생/로그인 이력 시드 후 baseline 측정
