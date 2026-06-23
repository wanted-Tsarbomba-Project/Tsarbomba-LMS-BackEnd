# Recommendation 도메인 — baseline/전후 비교 템플릿 (7단계)

> 프로세스 [`../../PROCESS.md`](../../PROCESS.md).
> 아직 baseline 측정 전이므로, 이 문서는 recommendation 도메인의 비교 기준과 결과 기입 위치를 먼저 고정한다.

## 측정 대상

| 트랙 | 대상 | 목적 |
|------|------|------|
| A. 사용자 조회 성능 | `GET /api/v1/recommendations/problem-sets/me` | 추천 목록 조회 p95와 DB query 병목 확인 |
| B. 배치/Python 병목 | `ProblemRecommendationGenerationService.generate()` | Python 호출 시간과 Java 저장 시간을 분리 |
| C. 추천 효과 관측 | 노출/클릭/score | 추천이 실제로 쓰이는지 실시간 신호 확인 |

---

## baseline 조건

| 항목 | 값 |
|------|----|
| 프로파일 | `loadtest` |
| DB | 도커 MySQL 3307 |
| 추천 목록 부하 | VU 50, 1~5분 |
| 추천 조회 데이터 | 사용자별 ACTIVE 추천 3개 이상, 완료 progress/숨김 row 혼합 |
| 배치 데이터 | Python mock 또는 loadtest FastAPI 응답으로 추천 대상 사용자 다수 |
| 성공 기준 | 추천 목록 p95 < 500ms, 실패율 < 1%, 배치 timeout 0건 |

---

## A. 사용자 조회 성능 비교

| 지표 | before | after | 판정 |
|---|---:|---:|---|
| k6 `http_req_duration{type=list}` p95 | 측정 전 | 측정 전 | - |
| k6 `http_req_waiting{type=list}` p95 | 측정 전 | 측정 전 | - |
| `recommendation_problem_set_list_duration` avg | 측정 전 | 측정 전 | - |
| `recommendation_problem_set_list_query_duration` avg | 측정 전 | 측정 전 | - |
| `recommendation_hide_lookup_duration` avg | 측정 전 | 측정 전 | - |
| `http_req_failed` | 측정 전 | 측정 전 | - |
| EXPLAIN rows/key | 측정 전 | 측정 전 | - |

확인할 변경 후보:

| 후보 | 기대 효과 |
|------|-----------|
| `problem_recommendation(user_id, status, rank_no, recommendation_id)` 계열 인덱스 검토 | 사용자별 ACTIVE 추천 정렬 조회 비용 감소 |
| `problem_progress(user_id, problem_set_id, is_completed)` 계열 인덱스 검토 | 완료 문제세트 제외 LEFT JOIN 비용 감소 |
| `recommendation_hide(user_id, hide_type, target_id)` 계열 인덱스 검토 | 숨김 조회 비용 감소 |

---

## B. 배치/Python 병목 비교

| 지표 | before | after | 판정 |
|---|---:|---:|---|
| `recommendation_generation_batch_duration` avg/max | 측정 전 | 측정 전 | - |
| `recommendation_generation_external_duration` avg/max | 측정 전 | 측정 전 | - |
| `recommendation_generation_save_duration` avg/max | 측정 전 | 측정 전 | - |
| generated user count | 측정 전 | 측정 전 | - |
| `recommendation_generation_failed_total` | 측정 전 | 측정 전 | - |
| Hikari active max | 측정 전 | 측정 전 | - |

해석표:

| 관찰 | 결론 |
|------|------|
| batch total ↑ + external ↑ + save 정상 | Python 추천 서버/네트워크/payload 병목 |
| batch total ↑ + external 정상 + save ↑ | Java DB 저장/락 병목 |
| external timeout 발생 | Python 서버 과부하 또는 timeout 설정 확인 |
| save ↑ + Hikari active ↑ | 추천 교체 저장의 DB write 병목 |

확인할 변경 후보:

| 후보 | 기대 효과 |
|------|-----------|
| Python 호출 Timer와 Java 저장 Timer 분리 | 병목 위치 명확화 |
| 사용자별 저장 batch 개선 | DB write 왕복 감소 |
| lock/deactivate 조회용 인덱스 검토 | 사용자별 ACTIVE 추천 교체 비용 감소 |
| Python mock 고정 지연 테스트 | 외부 지연 상황에서 Java 배치 한계 측정 |

---

## C. 추천 효과 관측 비교

| 지표 | before | after | 판정 |
|---|---:|---:|---|
| `recommendation_problem_set_exposed_total` | 구현 전 | 구현 후 | - |
| `recommendation_problem_set_clicked_total` | 구현 전 | 구현 후 | - |
| CTR(clicked/exposed) | 구현 전 | 구현 후 | - |
| confidence avg/p95 | 측정 전 | 측정 전 | - |
| lift avg/p95 | 측정 전 | 측정 전 | - |
| support avg/p95 | 측정 전 | 측정 전 | - |

주의:

- 추천 클릭/문제 진입 비율은 현재 API만으로 정확히 추론하기 어렵다.
- 프론트 클릭 이벤트 API 또는 문제 진입 `source=recommendation` 같은 명시적 이벤트가 필요하다.
- Grafana는 실시간 효과 신호 확인에 적합하고, 장기 코호트/학습 완료율/A-B 테스트는 BI 또는 분석 DB가 더 적합하다.

---

## 결론 기입 위치

baseline 측정 후 아래 형식으로 결론을 쓴다.

```text
결론: recommendation 도메인의 주 병목은 <추천 목록 query / Python external call / DB save / 추천 효과 이벤트 부재> 이다.
근거: k6 p95, custom timer, Hikari active, Loki durationMs, generated user count가 함께 설명한다.
```
