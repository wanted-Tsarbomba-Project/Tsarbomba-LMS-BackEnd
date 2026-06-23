# Recommendation 도메인 — 병목 가설 (7단계 §1단계)

> 프로세스: [`../../PROCESS.md`](../../PROCESS.md) · 컨벤션: [`../../CONVENTION.md`](../../CONVENTION.md)
> Recommendation 도메인은 단순 API 성능뿐 아니라 추천 생성 배치, Python 호출, 추천 효과 신호까지 함께 본다.

## 도메인 유형 분류

추천 도메인은 세 트랙으로 나눠 관측한다.

| 트랙 | 대상 | 유형 | 6단계 처리 방식 |
|------|------|------|------------------|
| **A. 사용자 조회 성능** | `GET /api/v1/recommendations/problem-sets/me` | 조회/집계형 | 숨김 조회 + 추천 목록 native join query의 인덱스/조인 비용 확인 |
| **B. 배치/Python 병목** | `ProblemRecommendationGenerationService.generate()` | 외부연동 + 쓰기/트랜잭션형 | Python FastAPI 호출 시간과 Java DB 저장 시간을 분리 측정 |
| **C. 추천 효과 관측** | 추천 노출/클릭/score 분포 | 제품 효과 관측 | Grafana로 실시간 효과 신호를 보고, 장기 분석은 BI/분석 DB 확장 후보로 둠 |

---

## 병목 가설표

| # | 대상 | 병목 가설 | 근거 (코드 위치) | 관찰 지표 | 성공 기준 |
|---|------|-----------|------------------|-----------|-----------|
| 1 ★ | `GET /api/v1/recommendations/problem-sets/me` | **추천 목록 조인/인덱스 병목.** 숨김 상태 조회 후 ACTIVE 추천, 문제세트, 카테고리, 완료 progress를 조인한다. 추천/진행 row가 많아지면 `user_id`, `status`, `rank_no`, `problem_set_id` 계열 인덱스가 없을 때 느려질 수 있다. | [`ProblemRecommendationQueryService.getRecommendations`](../../../../src/main/java/com/wanted/codebombalms/recommendation/application/service/ProblemRecommendationQueryService.java), [`SpringDataProblemRecommendationRepository.findActiveProblemSetRecommendations`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/persistence/SpringDataProblemRecommendationRepository.java) | HTTP p95, `recommendation_problem_set_list_query_duration`, EXPLAIN rows/key, Hikari active | VU 50, 추천 row 대량 시드 기준 p95 < 500ms, 실패율 < 1% |
| 2 ★ | `ProblemRecommendationGenerationService.generate()` | **Python FastAPI 호출이 배치 시간을 지배할 수 있음.** Java 배치는 `.block()`으로 Python 응답을 기다린 뒤 다음 저장 단계로 넘어간다. Python 계산/네트워크/응답 크기가 전체 시간의 주요 원인일 수 있다. | [`FastApiProblemRecommendationGenerationClient.generateProblemSetRecommendations`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/client/FastApiProblemRecommendationGenerationClient.java), [`ProblemRecommendationGenerationService.generate`](../../../../src/main/java/com/wanted/codebombalms/recommendation/application/service/ProblemRecommendationGenerationService.java) | `recommendation_generation_external_duration`, batch total duration, timeout/error 로그, 응답 userCount | Python 호출 timeout 0건, 외부 호출 시간이 허용 범위 내, batch total과 external 시간을 분리 설명 가능 |
| 3 ★ | `replaceActiveRecommendations()` | **사용자별 추천 교체 저장 반복 비용.** Python 응답 사용자마다 lock → deactivate → saveAll을 반복한다. 대상 사용자가 많으면 DB write/lock 비용과 Hikari 사용량이 증가할 수 있다. | [`ProblemRecommendationCommandAdapter.replaceActiveRecommendations`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/persistence/ProblemRecommendationCommandAdapter.java) | `recommendation_generation_save_duration`, Hikari active, lock wait, generated user count | 사용자 수 증가 대비 저장 시간이 과도하게 증가하지 않을 것 |
| 4 | `POST /api/v1/recommendations/problem-sets/hide-today` | 단순 upsert라 병목 가능성은 낮다. 다만 `recommendation_hide(user_id, hide_type, target_id)` 조회 인덱스가 없으면 숨김 조회/저장 비용이 커질 수 있다. | [`RecommendationHideService.hideToday`](../../../../src/main/java/com/wanted/codebombalms/recommendation/application/service/RecommendationHideService.java), [`SpringDataRecommendationHideRepository.findByUserIdAndHideTypeAndTargetIdIsNull`](../../../../src/main/java/com/wanted/codebombalms/recommendation/infrastructure/persistence/SpringDataRecommendationHideRepository.java) | hide lookup Timer, HTTP p95 | p95 < 300ms, 실패율 < 1% |
| 5 | 추천 효과 관측 | **추천이 실제로 쓰이는지 알 수 있는 이벤트가 부족함.** 추천 조회는 있지만 추천 카드 클릭/문제 진입 source 이벤트가 없으면 CTR을 정확히 볼 수 없다. | 현재 추천 조회 API와 문제 진입 API 사이에 추천 클릭 이벤트 없음 | `recommendation_problem_set_exposed_total`, `recommendation_problem_set_clicked_total`, CTR | 2차 확장: 노출 대비 클릭률/문제 시작률을 Grafana에서 추세로 확인 |

★ = recommendation 도메인 메인 측정 후보.

---

## 트랙 A. 사용자 조회 성능

추천 목록 API는 사용자 화면에 직접 노출되므로 p95를 기준으로 본다.

- 숨김 상태면 추천 DB 조회를 건너뛰는지 확인한다.
- 숨김 상태가 아니면 native query의 조인/LEFT JOIN anti join 비용을 확인한다.
- `limit`은 최대 3이라 응답 row는 작지만, source table이 커지면 인덱스 영향이 커질 수 있다.

---

## 트랙 B. 배치/Python 병목

추천 배치는 아래 구간을 반드시 분리해서 본다.

```text
Scheduler
→ DB named lock
→ Java → Python FastAPI 호출
→ 응답 검증
→ 사용자별 추천 교체 저장
```

해석 기준:

| 관찰 | 해석 |
|------|------|
| batch total ↑ + external duration ↑ + save 정상 | Python/네트워크/응답 크기 병목 |
| batch total ↑ + external 정상 + save duration ↑ | Java DB 저장/락 병목 |
| timeout/error 증가 | Python 서버 과부하 또는 연결 문제 |
| userCount 증가와 external 급증 | Python 알고리즘 계산량 또는 payload 병목 |

---

## 트랙 C. 추천 효과 관측

Grafana에서도 추천 효과의 실시간 신호는 볼 수 있다. 단, 장기 코호트/A-B 분석은 BI나 분석 DB가 더 적합하다.

| 관측 항목 | Grafana 적합도 | 필요 이벤트/메트릭 |
|----------|----------------|--------------------|
| 추천 노출 수 | 높음 | `recommendation_problem_set_exposed_total` |
| 추천 클릭 수 | 높음 | `recommendation_problem_set_clicked_total` |
| 추천 CTR | 높음 | clicked / exposed |
| 추천 score 분포 | 중간 | support/confidence/lift DistributionSummary |
| 장기 완료율/리텐션 | 낮음~중간 | 별도 분석 DB/BI 권장 |

---

## 다음 단계

- **2단계**: 추천 조회, Python 호출, 저장, 효과 관측 메트릭/로그 설계 → [`metrics.md`](metrics.md)
- **4·5단계**: 추천 row/진행 row/숨김 row 시드 후 조회 baseline, Python mock 또는 실제 loadtest FastAPI로 배치 baseline 측정
