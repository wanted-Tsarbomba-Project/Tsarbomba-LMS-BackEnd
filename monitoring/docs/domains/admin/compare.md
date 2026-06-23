# Admin 도메인 — baseline/전후 비교 템플릿 (7단계)

> 프로세스 [`../../PROCESS.md`](../../PROCESS.md).
> 아직 baseline 측정 전이므로, 이 문서는 admin 도메인의 비교 기준과 결과 기입 위치를 먼저 고정한다.

## 측정 대상

| 트랙 | 대상 | 목적 |
|------|------|------|
| A. 목록 조회 | `GET /api/v1/admin/operation-alerts` | alert 대량 상황에서 필터/정렬 query 병목 확인 |
| B. 상세 조회 | `GET /api/v1/admin/operation-alerts/{operationAlertId}` | target type별 상세 조합 비용 확인 |
| C. 자동화 실행 | `OperationRuleExecutionService.run()` | USER N+1, 집계 query, alert upsert 반복 비용 확인 |

---

## baseline 조건

| 항목 | 값 |
|------|----|
| 프로파일 | `loadtest` |
| DB | 도커 MySQL 3307 |
| 목록 조회 부하 | VU 30~50, 1~5분 |
| 상세 조회 부하 | target type별 대표 alert ID 순환 |
| 자동화 실행 데이터 | 학생/로그인 이력/강좌/수강신청/제출/alert 대량 시드 |
| 성공 기준 | 목록 p95 < 500~800ms, 상세 p95 < 300~500ms, 실패율 < 1% |

---

## A. 운영 알림 목록 조회 비교

| 지표 | before | after | 판정 |
|---|---:|---:|---|
| k6 `http_req_duration{type=list}` p95 | 측정 전 | 측정 전 | - |
| k6 `http_req_waiting{type=list}` p95 | 측정 전 | 측정 전 | - |
| `admin_operation_alert_list_query_duration` avg | 측정 전 | 측정 전 | - |
| `http_req_failed` | 측정 전 | 측정 전 | - |
| EXPLAIN rows/key | 측정 전 | 측정 전 | - |

확인할 변경 후보:

| 후보 | 기대 효과 |
|------|-----------|
| `operation_alert(deleted_at, target_type, status, last_detected_at, operation_alert_id)` 계열 인덱스 검토 | 필터 + 정렬 비용 감소 |
| 목록 응답 projection 유지 | 과조회 방지 |

---

## B. 운영 알림 상세 조회 비교

| targetType | before p95 | after p95 | 주요 병목 후보 |
|------------|-----------:|----------:|----------------|
| `COURSE` | 측정 전 | 측정 전 | course 상세 + instructor 조회 |
| `PROBLEM` | 측정 전 | 측정 전 | problem/problemSet 상세 + creator 조회 |
| `USER` | 측정 전 | 측정 전 | student detail 조회 |

확인할 변경 후보:

| 후보 | 기대 효과 |
|------|-----------|
| target detail 구간 Timer 분리 | 어떤 target type이 느린지 식별 |
| 상세 조회 projection 유지 | alert + rule 기본 정보 과조회 방지 |

---

## C. 자동화 실행 비교

| 지표 | before | after | 판정 |
|---|---:|---:|---|
| `admin_operation_rule_run_duration` avg/max | 측정 전 | 측정 전 | - |
| `admin_operation_rule_detect_duration{ruleCode=USER_INACTIVE_NO_COURSE}` avg | 측정 전 | 측정 전 | - |
| SQL count per run | 측정 전 | 측정 전 | - |
| `admin_operation_alert_upsert_duration` avg | 측정 전 | 측정 전 | - |
| Hikari active max | 측정 전 | 측정 전 | - |
| detectedCount | 측정 전 | 측정 전 | - |

확인할 변경 후보:

| 후보 | 기대 효과 |
|------|-----------|
| 학생별 최신 로그인 조회 batch/집계 query | USER 규칙 N+1 제거 |
| alert open 상태 조회용 복합 인덱스 | `findOpenByRuleIdAndTarget` 반복 비용 감소 |
| rule detect와 alert save 구간 분리 | 병목 위치 명확화 |

---

## 결론 기입 위치

baseline 측정 후 아래 형식으로 결론을 쓴다.

```text
결론: admin 도메인의 주 병목은 <목록 조회 / 상세 target detail / 자동화 USER rule / alert upsert> 이다.
근거: k6 p95, custom timer, Hikari active, Loki durationMs, SQL count가 함께 증가했다.
```
