# Admin 도메인 — 성공 기준 (7단계 §3단계)

> 프로세스 [`../../PROCESS.md`](../../PROCESS.md) · 컨벤션 [`../../CONVENTION.md`](../../CONVENTION.md)
> 로컬 `loadtest` 프로파일, 도커 MySQL 3307, 단일 Spring Boot 앱 기준이다.

## 병목 가설별 측정 방식

| 가설 | 대상 | 측정 방식 | 산출물 |
|------|------|-----------|--------|
| #1 USER 규칙 N+1 | `OperationRuleExecutionService.run()` | loadtest trigger API로 스케줄러 본체 실행 | `monitoring-local/k6/scripts/admin/03-operation-rule-run-baseline.js`, `admin_operation_rule_detect_duration{ruleCode="USER_INACTIVE_NO_COURSE"}` |
| #2 alert upsert 반복 비용 | `OperationRuleExecutionService.saveAlert()` | loadtest trigger API로 자동화 실행 후 탐지 결과 수와 upsert timer 관찰 | `admin_operation_alert_upsert_duration`, `admin_operation_rule_detected_total` |
| #3 목록 query/정렬 | `GET /api/v1/admin/operation-alerts` | k6 목록 단독 baseline | `monitoring-local/k6/scripts/admin/01-alert-list-baseline.js` |
| #4 상세 + target detail | `GET /api/v1/admin/operation-alerts/{operationAlertId}` | k6 상세 단독 baseline | `monitoring-local/k6/scripts/admin/02-alert-detail-baseline.js` |

---

## k6 대상과 트래픽 조건

| 스크립트 | 대상 | 유형 | 트래픽 조건 | 측정 기간 |
|----------|------|------|-------------|-----------|
| `01-alert-list-baseline.js` | `GET /api/v1/admin/operation-alerts?page=0&size=20` | 조회/집계형 | 0→50 VU ramp-up 20s, 50 VU 유지 40s, ramp-down 10s | 총 70s |
| `02-alert-detail-baseline.js` | `GET /api/v1/admin/operation-alerts/{operationAlertId}` | 단건 조회 + target detail 조합 | 0→50 VU ramp-up 20s, 50 VU 유지 40s, ramp-down 10s | 총 70s |
| `03-operation-rule-run-baseline.js` | `POST /internal/loadtest/admin/operation-rules/run` | 스케줄러/배치 실행형 | 기본 1 VU, 1 iteration | 1회 실행 기준 |

전제:

- `LOGIN_EMAIL=admin@test.com`, `LOGIN_PASSWORD=Test1234!`를 사용한다.
- loadtest DB에 admin 계정, `RULE_MANAGEMENT` 권한, 운영 알림 200건이 seed 되어 있어야 한다.
- 자동화 trigger baseline은 추가로 휴면 사용자 240명, 저수강 강의 40개, 문제 제출 2,000건이 seed 되어 있어야 한다.
- 상세 조회는 `OPERATION_ALERT_ID` 환경변수로 대상 알림을 고정한다. 기본 seed 기준 `1`은 COURSE target이다.

---

## 목록 조회 Threshold

```javascript
export const options = {
    stages: [
        { duration: "20s", target: 50 },
        { duration: "40s", target: 50 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:list}": ["p(95)<800"],
    },
};
```

## 상세 조회 Threshold

```javascript
export const options = {
    stages: [
        { duration: "20s", target: 50 },
        { duration: "40s", target: 50 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:detail}": ["p(95)<500"],
    },
};
```

## 자동화 trigger Threshold

```javascript
export const options = {
    vus: 1,
    iterations: 1,
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:operation_rule_run}": ["p(95)<10000"],
    },
};
```

---

## 중단 기준

| 조건 | 판단 |
|------|------|
| `http_req_failed >= 1%` | 테스트 데이터/권한/서버 오류 확인 후 중단 |
| 목록 p95 1.5s 이상 지속 | 목록 query/정렬/인덱스 병목 우선 확인 |
| 상세 p95 1s 이상 지속 | alert 기본 조회와 target detail timer를 분리 확인 |
| 상세 조회 404 반복 | `OPERATION_ALERT_ID` 또는 target domain seed 정합성 확인 |
| 자동화 trigger 10s 이상 지속 | rule detect와 alert upsert 중 어느 timer가 높은지 분리 확인 |
| Hikari active가 풀 상한에 근접 | DB 커넥션 경합 가능성 확인 |

---

## 비즈니스 실패와 시스템 장애 구분

| 응답 | 해석 |
|------|------|
| `200` | 정상 성공 |
| `400` invalid page | k6 스크립트 오류 |
| `401` / `403` | admin 로그인 계정 또는 권한 seed 문제 |
| `404` detail not found | 상세 조회 seed 데이터 문제 |
| `5xx` | 시스템 장애로 실패율에 포함 |

---

## 관찰 지표

| 가설 | 도구 | 지표 |
|------|------|------|
| #3 목록 | k6 | `http_req_duration{type=list}`, `http_req_failed` |
| #3 목록 | Prometheus | `http_server_requests_seconds{domain="admin", uri="/api/v1/admin/operation-alerts"}` |
| #3 목록 | Prometheus | `admin_operation_alert_list_query_duration_seconds_*` |
| #3 목록 | Loki | `event=admin_operation_alert_list_queried resultCount=... totalElements=... durationMs=...` |
| #4 상세 | k6 | `http_req_duration{type=detail}`, `http_req_failed` |
| #4 상세 | Prometheus | `admin_operation_alert_detail_query_duration_seconds_*` |
| #4 상세 | Prometheus | `admin_operation_alert_target_detail_duration_seconds_*` |
| #4 상세 | Loki | `event=admin_operation_alert_detail_queried`, `event=admin_operation_alert_target_detail_loaded` |
| #1·#2 자동화 | Prometheus | `admin_operation_rule_run_duration_seconds_*`, `admin_operation_rule_detect_duration_seconds_*`, `admin_operation_alert_upsert_duration_seconds_*` |
| #1·#2 자동화 | Loki | `event=admin_operation_rule_run_completed`, `event=admin_operation_rule_detected`, `event=admin_operation_alert_upserted` |

## 결론 기준

| 결과 | 판단 |
|------|------|
| 목록 p95 통과 + list query timer 안정 | 목록 baseline 기준 충족 |
| 목록 HTTP p95 상승 + list query timer 상승 | 목록 query/정렬 병목 |
| 상세 p95 통과 + detail/target timer 안정 | 상세 baseline 기준 충족 |
| 상세 p95 상승 + target detail timer 상승 | target type별 외부 도메인 조회 병목 |
| rule run 상승 + detect/upsert timer 상승 | 자동화 규칙 또는 alert upsert 병목 |
