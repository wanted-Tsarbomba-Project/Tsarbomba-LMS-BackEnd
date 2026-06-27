# Admin 도메인 — 심은 메트릭/로그 (7단계 §2단계)

> 프로세스 [`../../PROCESS.md`](../../PROCESS.md) · 컨벤션 [`../../CONVENTION.md`](../../CONVENTION.md)
> admin 도메인은 A. 운영 알림 조회, B. target detail 조합, C. 자동화 규칙 실행/alert upsert를 분리해서 관측한다.

## 커스텀 메트릭 (Layer 3)

| Prometheus 노출명 | 종류 | 코드 등록명 | 의미 | 코드 위치 |
|---|---|---|---|---|
| `admin_operation_alert_list_query_duration_seconds_{count,sum,max}` | Timer | `admin_operation_alert_list_query_duration` | 운영 알림 목록 조회 DB 구간 시간 | [`AdminMetrics`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/metrics/AdminMetrics.java), [`OperationAlertQueryService.getAlerts`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/alert/application/service/OperationAlertQueryService.java) |
| `admin_operation_alert_detail_query_duration_seconds_{count,sum,max}` | Timer | `admin_operation_alert_detail_query_duration` | 운영 알림 상세 기본 정보 조회 시간 | [`AdminMetrics`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/metrics/AdminMetrics.java), [`OperationAlertQueryService.getAlertDetail`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/alert/application/service/OperationAlertQueryService.java) |
| `admin_operation_alert_target_detail_duration_seconds_{count,sum,max}` | Timer | `admin_operation_alert_target_detail_duration` | `COURSE`/`PROBLEM`/`USER` 대상 상세 조합 시간 | [`AdminMetrics`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/metrics/AdminMetrics.java), [`OperationAlertTargetDetailAdapter.loadTargetDetail`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/alert/infrastructure/adapter/OperationAlertTargetDetailAdapter.java) |
| `admin_operation_rule_run_duration_seconds_{count,sum,max}` | Timer | `admin_operation_rule_run_duration` | 활성화된 운영 자동화 규칙 전체 실행 시간 | [`AdminMetrics`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/metrics/AdminMetrics.java), [`OperationRuleExecutionService.run`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/automation/application/service/OperationRuleExecutionService.java) |
| `admin_operation_rule_detect_duration_seconds_{count,sum,max}` | Timer | `admin_operation_rule_detect_duration` | 규칙별 handler 탐지 실행 시간 | [`AdminMetrics`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/metrics/AdminMetrics.java), [`OperationRuleExecutionService.executeRule`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/automation/application/service/OperationRuleExecutionService.java) |
| `admin_operation_alert_upsert_duration_seconds_{count,sum,max}` | Timer | `admin_operation_alert_upsert_duration` | 탐지 결과 1건의 open alert 조회 + 저장 시간 | [`AdminMetrics`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/metrics/AdminMetrics.java), [`OperationRuleExecutionService.saveAlert`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/automation/application/service/OperationRuleExecutionService.java) |
| `admin_operation_rule_detected_total` | Counter | `admin_operation_rule_detected_total` | 규칙별 탐지 결과 누적 수 | [`AdminMetrics`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/metrics/AdminMetrics.java), [`OperationRuleExecutionService.executeRule`](../../../../src/main/java/com/wanted/codebombalms/admin/operation/automation/application/service/OperationRuleExecutionService.java) |

## Metric Tag

| 메트릭 | tag | 허용 값 |
|--------|-----|---------|
| `admin_operation_alert_target_detail_duration` | `targetType` | `COURSE`, `PROBLEM`, `USER` |
| `admin_operation_rule_detect_duration` | `ruleCode` | `USER_INACTIVE_NO_COURSE`, `COURSE_LOW_ENROLLMENT`, `PROBLEM_HIGH_WRONG_RATE` |
| `admin_operation_rule_detected_total` | `ruleCode` | enum 값 |

`operationAlertId`, `targetId`, `userId`, `traceId`는 metric tag에 넣지 않는다.

---

## 구조화 로그 (Loki)

```text
event=admin_operation_alert_list_queried targetType=<type|null> status=<status|null> resultCount=<n> totalElements=<n> durationMs=<n>
event=admin_operation_alert_detail_queried targetType=<type> durationMs=<n>
event=admin_operation_alert_target_detail_loaded targetType=<type> durationMs=<n>
event=admin_operation_rule_run_completed enabledRuleCount=<n> detectedCount=<n> durationMs=<n>
event=admin_operation_rule_detected ruleCode=<code> detectedCount=<n> durationMs=<n>
event=admin_operation_alert_upserted ruleCode=<code> targetType=<type> alertCount=1 durationMs=<n>
```

`traceId`는 `MdcLoggingFilter`가 MDC로 자동 부착한다.

---

## PromQL / LogQL 후보

### 운영 알림 목록 조회 평균

```promql
rate(admin_operation_alert_list_query_duration_seconds_sum[1m])
/
rate(admin_operation_alert_list_query_duration_seconds_count[1m])
```

### target type별 상세 조합 평균

```promql
sum by (targetType) (rate(admin_operation_alert_target_detail_duration_seconds_sum[1m]))
/
sum by (targetType) (rate(admin_operation_alert_target_detail_duration_seconds_count[1m]))
```

### 규칙별 탐지 구간 평균

```promql
sum by (ruleCode) (rate(admin_operation_rule_detect_duration_seconds_sum[1m]))
/
sum by (ruleCode) (rate(admin_operation_rule_detect_duration_seconds_count[1m]))
```

### 규칙별 탐지 결과 수

```promql
sum by (ruleCode) (increase(admin_operation_rule_detected_total[5m]))
```

### 느린 admin 이벤트 로그

```logql
{job="lms"} |= "event=admin_operation_rule_detected"
  | regexp "durationMs=(?P<durationMs>[0-9]+)" | unwrap durationMs
```

---

## 해석 기준

| 관찰 | 해석 |
|------|------|
| 목록 API p95 상승 + `admin_operation_alert_list_query_duration` 상승 | 운영 알림 목록 query/정렬 병목 |
| 상세 API p95 상승 + `target_detail_duration{targetType=...}` 특정 타입만 상승 | 해당 target 도메인 조합 조회 병목 |
| `rule_run_duration` 상승 + 특정 `rule_detect_duration{ruleCode=...}` 상승 | 특정 자동화 규칙 handler 병목 |
| `rule_run_duration` 상승 + `alert_upsert_duration` 상승 + Hikari active 상승 | alert upsert 반복 또는 DB write/lock 병목 |
| `rule_detected_total` 증가와 upsert 시간이 같이 증가 | 탐지 결과 수 증가가 저장 비용을 키우는 상황 |

`admin_operation_alert_upsert_duration`은 사용 중인 metric이다. `OperationRuleExecutionService.saveAlert()`에서 탐지 결과 1건마다 `recordAlertUpsert()`를 호출해 open alert 조회 + 저장 시간을 기록한다.
