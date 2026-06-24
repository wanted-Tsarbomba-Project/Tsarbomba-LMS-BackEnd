# Recommendation 도메인 — 심은 메트릭/로그 (7단계 §2단계)

> 프로세스 [`../../PROCESS.md`](../../PROCESS.md) · 컨벤션 [`../../CONVENTION.md`](../../CONVENTION.md)
> 추천 도메인은 A. 사용자 조회 성능, B. 배치/Python 병목, C. 추천 효과 관측을 분리해서 관측한다.

## A. 사용자 조회 성능 메트릭

| Prometheus 노출명 | 종류 | 코드 등록명 | 의미 | 코드 위치 |
|---|---|---|---|---|
| `recommendation_problem_set_list_duration_seconds_{count,sum,max}` | Timer | `recommendation_problem_set_list_duration` | 추천 목록 조회 전체 시간(숨김 확인 포함) | [`RecommendationMetrics`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/metrics/RecommendationMetrics.java), [`ProblemRecommendationQueryService.getRecommendations`](../../../../src/main/java/com/wanted/codebombalms/recommendation/application/service/ProblemRecommendationQueryService.java) |
| `recommendation_hide_lookup_duration_seconds_{count,sum,max}` | Timer | `recommendation_hide_lookup_duration` | 추천 숨김 상태 조회 시간 | [`RecommendationMetrics`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/metrics/RecommendationMetrics.java), [`RecommendationHidePersistenceAdapter.findHide`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/persistence/RecommendationHidePersistenceAdapter.java) |
| `recommendation_problem_set_list_query_duration_seconds_{count,sum,max}` | Timer | `recommendation_problem_set_list_query_duration` | ACTIVE 추천 목록 native query 시간 | [`RecommendationMetrics`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/metrics/RecommendationMetrics.java), [`ProblemRecommendationQueryAdapter.findActiveProblemSetRecommendations`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/persistence/ProblemRecommendationQueryAdapter.java) |

### 로그

```text
event=recommendation_problem_set_list_queried hidden=<true|false> resultCount=<n> durationMs=<n>
event=recommendation_hide_lookup_completed hidden=<true|false> durationMs=<n>
event=recommendation_problem_set_list_query_completed resultCount=<n> durationMs=<n>
```

---

## B. 배치/Python 병목 메트릭

| Prometheus 노출명 | 종류 | 코드 등록명 | 의미 | 코드 위치 |
|---|---|---|---|---|
| `recommendation_generation_batch_duration_seconds_{count,sum,max}` | Timer | `recommendation_generation_batch_duration` | 추천 생성 배치 전체 시간 | [`RecommendationMetrics`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/metrics/RecommendationMetrics.java), [`ProblemRecommendationGenerationService.generate`](../../../../src/main/java/com/wanted/codebombalms/recommendation/application/service/ProblemRecommendationGenerationService.java) |
| `recommendation_generation_external_duration_seconds_{count,sum,max}` | Timer | `recommendation_generation_external_duration` | Java → Python FastAPI 호출 round-trip 시간 | [`RecommendationMetrics`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/metrics/RecommendationMetrics.java), [`FastApiProblemRecommendationGenerationClient.generateProblemSetRecommendations`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/client/FastApiProblemRecommendationGenerationClient.java) |
| `recommendation_generation_save_duration_seconds_{count,sum,max}` | Timer | `recommendation_generation_save_duration` | 사용자별 추천 교체 저장 시간 | [`RecommendationMetrics`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/metrics/RecommendationMetrics.java), [`ProblemRecommendationCommandAdapter.replaceActiveRecommendations`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/persistence/ProblemRecommendationCommandAdapter.java) |
| `recommendation_generation_user_total` | Counter | `recommendation_generation_user_total` | 추천이 생성된 사용자 수 누적 | [`ProblemRecommendationGenerationService.generate`](../../../../src/main/java/com/wanted/codebombalms/recommendation/application/service/ProblemRecommendationGenerationService.java) |
| `recommendation_generation_failed_total` | Counter | `recommendation_generation_failed_total` | 추천 생성 실패 수 | [`ProblemRecommendationGenerationService`](../../../../src/main/java/com/wanted/codebombalms/recommendation/application/service/ProblemRecommendationGenerationService.java), [`FastApiProblemRecommendationGenerationClient`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/client/FastApiProblemRecommendationGenerationClient.java) |

### Metric Tag

| 메트릭 | tag | 허용 값 |
|--------|-----|---------|
| `recommendation_generation_save_duration` | `status` | `success`, `failed` |
| `recommendation_generation_failed_total` | `reason` | `timeout`, `external_error`, `invalid_response`, `save_error` |

`userId`, `problemSetId`, exception message는 metric tag에 넣지 않는다.

### 로그

```text
event=recommendation_generation_batch_completed userCount=<n> durationMs=<n>
event=recommendation_generation_external_called userCount=<n> durationMs=<n>
event=recommendation_generation_saved userId=<id> itemCount=<n> durationMs=<n>
event=recommendation_generation_failed reason=<reason> exceptionType=<type>
```

---

## C. 추천 효과 관측 메트릭

| Prometheus 노출명 | 종류 | 코드 등록명 | 의미 | 코드 위치 |
|---|---|---|---|---|
| `recommendation_problem_set_exposed_total` | Counter | `recommendation_problem_set_exposed_total` | 추천 목록에서 사용자에게 노출된 추천 카드 수 | [`ProblemRecommendationQueryService.getRecommendations`](../../../../src/main/java/com/wanted/codebombalms/recommendation/application/service/ProblemRecommendationQueryService.java) |
| `recommendation_score_confidence_{count,sum,max}` | DistributionSummary | `recommendation_score_confidence` | 생성된 추천 confidence 분포 | [`ProblemRecommendationCommandAdapter.replaceActiveRecommendations`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/persistence/ProblemRecommendationCommandAdapter.java) |
| `recommendation_score_lift_{count,sum,max}` | DistributionSummary | `recommendation_score_lift` | 생성된 추천 lift 분포 | [`ProblemRecommendationCommandAdapter.replaceActiveRecommendations`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/persistence/ProblemRecommendationCommandAdapter.java) |
| `recommendation_score_support_{count,sum,max}` | DistributionSummary | `recommendation_score_support` | 생성된 추천 support 분포 | [`ProblemRecommendationCommandAdapter.replaceActiveRecommendations`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/persistence/ProblemRecommendationCommandAdapter.java) |

`recommendation_problem_set_clicked_total`은 아직 클릭 이벤트 API/source 파라미터가 없어 이번 2단계에서는 심지 않았다.

---

## PromQL / LogQL 후보

### 추천 목록 query 평균

```promql
rate(recommendation_problem_set_list_query_duration_seconds_sum[1m])
/
rate(recommendation_problem_set_list_query_duration_seconds_count[1m])
```

### Python 호출 평균

```promql
rate(recommendation_generation_external_duration_seconds_sum[1m])
/
rate(recommendation_generation_external_duration_seconds_count[1m])
```

### 저장 상태별 평균

```promql
sum by (status) (rate(recommendation_generation_save_duration_seconds_sum[1m]))
/
sum by (status) (rate(recommendation_generation_save_duration_seconds_count[1m]))
```

### 실패 사유별 증가량

```promql
sum by (reason) (increase(recommendation_generation_failed_total[5m]))
```

### 추천 노출 수

```promql
sum(increase(recommendation_problem_set_exposed_total[5m]))
```

### 배치 실패 로그

```logql
{job="lms"} |= "event=recommendation_generation_failed"
```

---

## 해석 기준

| 관찰 | 해석 |
|------|------|
| 추천 목록 p95 상승 + `problem_set_list_query_duration` 상승 | 추천 native query/조인/인덱스 병목 |
| `problem_set_list_duration` 상승 + `hide_lookup_duration`만 상승 | 숨김 조회 병목 |
| batch total 상승 + external duration 상승 | Python/FastAPI/네트워크 병목 |
| batch total 상승 + save duration 상승 | Java DB 저장/락 병목 |
| `generation_failed_total{reason="timeout"}` 증가 | Python 응답 지연 또는 연결 timeout |
| exposed 증가 대비 클릭 이벤트 없음 | CTR은 후속 클릭 이벤트 API/source 파라미터 도입 후 측정 |
