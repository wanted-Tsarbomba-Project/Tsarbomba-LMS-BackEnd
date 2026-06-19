# Recommendation 도메인 — 성공 기준 (7단계 §3단계)

> 프로세스 [`../../PROCESS.md`](../../PROCESS.md) · 컨벤션 [`../../CONVENTION.md`](../../CONVENTION.md)
> 로컬 `loadtest` 프로파일, 도커 MySQL 3307, 단일 Spring Boot 앱 기준이다.

## 병목 가설별 측정 방식

| 가설 | 대상 | 측정 방식 | 산출물 |
|------|------|-----------|--------|
| #1 추천 목록 조인/인덱스 | `GET /api/v1/recommendations/problem-sets/me` | k6 목록 단독 baseline | `monitoring/k6/scripts/recommendation/01-list-baseline.js` |
| #2 Python FastAPI 호출 | `ProblemRecommendationGenerationService.generate()` | loadtest trigger API로 배치 본체 실행 | `monitoring/k6/scripts/recommendation/03-generation-baseline.js`, `recommendation_generation_external_duration`, `recommendation_generation_batch_duration` |
| #3 추천 교체 저장 반복 | `ProblemRecommendationCommandAdapter.replaceActiveRecommendations()` | loadtest trigger API 실행 중 save timer 관찰 | `recommendation_generation_save_duration{status=...}` |
| #4 hide today upsert | `POST /api/v1/recommendations/problem-sets/hide-today` | k6 hide 단독 baseline | `monitoring/k6/scripts/recommendation/02-hide-today-baseline.js` |

---

## k6 대상과 트래픽 조건

| 스크립트 | 대상 | 유형 | 트래픽 조건 | 측정 기간 |
|----------|------|------|-------------|-----------|
| `01-list-baseline.js` | `GET /api/v1/recommendations/problem-sets/me?limit=3` | 조회/집계형 | 0→50 VU ramp-up 20s, 50 VU 유지 40s, ramp-down 10s | 총 70s |
| `02-hide-today-baseline.js` | `POST /api/v1/recommendations/problem-sets/hide-today` | 단순 쓰기/upsert | 0→30 VU ramp-up 20s, 30 VU 유지 40s, ramp-down 10s | 총 70s |
| `03-generation-baseline.js` | `POST /internal/loadtest/recommendations/generate` | 추천 배치 실행형 | 기본 1 VU, 1 iteration | 1회 실행 기준 |

전제:

- `LOGIN_EMAIL=u01@test.com`, `LOGIN_PASSWORD=Test1234!`를 사용한다.
- 추천 목록 baseline에서는 hide API를 호출하지 않는다.
- hide baseline은 목록 baseline과 분리해서 실행한다. hide 실행 후에는 목록 API가 `hidden=true`가 되어 추천 native query를 건너뛴다.
- loadtest DB에 `problem_recommendation`, `problem_set`, `problem_category`, `problem_progress` row가 충분히 있어야 한다.
- 배치 baseline에서는 Java 앱의 `fastapi.url`이 추천 FastAPI 서버를 바라봐야 하고, Python 서버도 같은 loadtest MySQL 데이터를 읽어야 한다.
- 배치용 seed는 추천 대상 사용자 120명, 사용자별 완료 progress 12건, 기존 ACTIVE 추천 3건을 만든다.

---

## 추천 목록 Threshold

```javascript
export const options = {
    stages: [
        { duration: "20s", target: 50 },
        { duration: "40s", target: 50 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:list}": ["p(95)<500"],
    },
};
```

## hide today Threshold

```javascript
export const options = {
    stages: [
        { duration: "20s", target: 30 },
        { duration: "40s", target: 30 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:hide}": ["p(95)<300"],
    },
};
```

## 추천 배치 trigger Threshold

```javascript
export const options = {
    vus: 1,
    iterations: 1,
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:recommendation_generation}": ["p(95)<300000"],
    },
};
```

---

## 중단 기준

| 조건 | 판단 |
|------|------|
| `http_req_failed >= 1%` | 인증/seed/서버 오류 확인 후 중단 |
| 추천 목록 p95 1s 이상 지속 | native query/인덱스 병목 우선 확인 |
| 목록 응답이 계속 `hidden=true` | hide seed 상태 확인. 목록 query baseline으로 부적합 |
| 목록 `problemSets`가 계속 빈 배열 | 추천 row seed 또는 완료 progress 조건 확인 |
| hide p95 800ms 이상 지속 | hide 조회/저장 upsert 병목 가능성 확인 |
| 배치 trigger 5분 이상 지속 | FastAPI 호출 timeout, Python 계산 시간, Java save timer를 분리 확인 |
| `generatedUserCount=0` 반복 | Python 서버가 3개 추천을 만들 수 있는 사용자를 찾지 못한 상태. progress/문제세트 seed와 Python DB 연결 확인 |

---

## 비즈니스 실패와 시스템 장애 구분

| 응답 | 해석 |
|------|------|
| `200` + `hidden=false` | 추천 목록 조회 baseline 정상 |
| `200` + `hidden=true` | 시스템 장애는 아니지만 목록 query 병목 측정에는 부적합 |
| `200` + hide response | hide upsert 정상 |
| `401` | 로그인 seed 문제 |
| `5xx` | 시스템 장애로 실패율에 포함 |

---

## 관찰 지표

| 가설 | 도구 | 지표 |
|------|------|------|
| #1 목록 | k6 | `http_req_duration{type=list}`, `http_req_failed` |
| #1 목록 | Prometheus | `http_server_requests_seconds{domain="recommendation"}` |
| #1 목록 | Prometheus | `recommendation_problem_set_list_duration_seconds_*` |
| #1 목록 | Prometheus | `recommendation_hide_lookup_duration_seconds_*` |
| #1 목록 | Prometheus | `recommendation_problem_set_list_query_duration_seconds_*` |
| #1 목록 | Loki | `event=recommendation_problem_set_list_queried`, `event=recommendation_problem_set_list_query_completed` |
| #4 hide | k6 | `http_req_duration{type=hide}`, `http_req_failed` |
| #4 hide | Prometheus | `http_server_requests_seconds{domain="recommendation"}` |
| #4 hide | Loki | `event=request_completed uri=/api/v1/recommendations/problem-sets/hide-today` |
| #2·#3 배치 | Prometheus | `recommendation_generation_batch_duration_seconds_*`, `recommendation_generation_external_duration_seconds_*`, `recommendation_generation_save_duration_seconds_*` |
| #2·#3 배치 | Loki | `event=recommendation_generation_batch_completed`, `event=recommendation_generation_external_called`, `event=recommendation_generation_saved` |

## 결론 기준

| 결과 | 판단 |
|------|------|
| 목록 p95 통과 + custom query timer 안정 | 추천 목록 baseline 기준 충족 |
| HTTP p95 상승 + list query timer 상승 | native query/인덱스 병목 |
| list duration 상승 + hide lookup만 상승 | 숨김 조회 병목 |
| batch total 상승 + external duration 상승 | Python/FastAPI/네트워크 병목 |
| batch total 상승 + save duration 상승 | Java DB 저장/락 병목 |
