# Recommendation Generation Scale Test

추천 생성 배치에서 Python FastAPI 내부 병목이 입력 규모에 따라 어떻게 증가하는지 확인하기 위한 절차다.

---

## 측정 구간

| 구간 | Metric |
|------|--------|
| Python DB 조회 | `recommendation_python_generation_stage_last_duration_seconds{stage="db_fetch"}` |
| transaction 구성 | `recommendation_python_generation_stage_last_duration_seconds{stage="transaction_build"}` |
| frequent itemset 계산 | `recommendation_python_generation_stage_last_duration_seconds{stage="frequent_itemset"}` |
| association rule 생성 | `recommendation_python_generation_stage_last_duration_seconds{stage="rule_generation"}` |
| 사용자별 추천 선택 | `recommendation_python_generation_stage_last_duration_seconds{stage="user_pick"}` |
| 응답 DTO 생성 | `recommendation_python_generation_stage_last_duration_seconds{stage="response_build"}` |
| Python 전체 | `recommendation_python_generation_stage_last_duration_seconds{stage="total"}` |

---

## 입력 규모 Metric

| 항목 | Metric |
|------|--------|
| 입력 사용자 수 | `recommendation_python_generation_scale{type="input_users"}` |
| transaction 수 | `recommendation_python_generation_scale{type="transactions"}` |
| 활성 문제셋 수 | `recommendation_python_generation_scale{type="active_problem_sets"}` |
| frequent itemset 수 | `recommendation_python_generation_scale{type="frequent_itemsets"}` |
| association rule 수 | `recommendation_python_generation_scale{type="association_rules"}` |
| 추천 생성 사용자 수 | `recommendation_python_generation_scale{type="generated_users"}` |

---

## 실행 전 조건

| 서버 | 조건 |
|------|------|
| Backend | `loadtest` profile, loadtest MySQL 사용 |
| Python | `http://localhost:8000`, 같은 loadtest MySQL 사용 |
| Prometheus | `host.docker.internal:8080/actuator/prometheus`, `host.docker.internal:8000/metrics` scrape |
| Grafana | `LMS Recommendation Load Test Dashboard` 확인 |

---

## Scale별 권장 실행

각 규모는 seed 데이터가 달라져야 하므로, 가장 단순한 방식은 scale별로 loadtest DB를 초기화하고 Backend를 해당 환경변수로 다시 실행하는 것이다.

| 테스트 | 환경변수 |
|--------|----------|
| 120명 | `LOADTEST_RECOMMENDATION_BATCH_USERS=120` |
| 300명 | `LOADTEST_RECOMMENDATION_BATCH_USERS=300` |
| 500명 | `LOADTEST_RECOMMENDATION_BATCH_USERS=500` |
| 1000명 | `LOADTEST_RECOMMENDATION_BATCH_USERS=1000` |

문제셋 수와 사용자별 완료 수를 같이 키우고 싶으면 아래 값도 조정한다.

| 환경변수 | 기본값 | 설명 |
|----------|--------|------|
| `LOADTEST_RECOMMENDATION_SETS` | `200` | 활성 문제셋 수 |
| `LOADTEST_RECOMMENDATION_COMPLETED_PER_USER` | `12` | 추천 대상 사용자별 완료 문제셋 수 |
| `LOADTEST_RECOMMENDATION_EXISTING_RECOMMENDATIONS_PER_USER` | `3` | 기존 ACTIVE 추천 수 |
| `LOADTEST_RECOMMENDATION_COMPLETED_EVERY` | `10` | 목록 조회용 로그인 사용자의 완료 progress 간격 |

---

## k6 실행

```bash
docker compose run --rm \
  -e RESULT_NAME=recommendation-generation-scale-120 \
  -e SCALE_USERS=120 \
  -e LOGIN_EMAIL=admin@test.com \
  -e LOGIN_PASSWORD=Test1234! \
  k6 run -o experimental-prometheus-rw /scripts/recommendation/03-generation-baseline.js
```

scale만 바꿔서 반복한다.

```bash
docker compose run --rm \
  -e RESULT_NAME=recommendation-generation-scale-300 \
  -e SCALE_USERS=300 \
  -e LOGIN_EMAIL=admin@test.com \
  -e LOGIN_PASSWORD=Test1234! \
  k6 run -o experimental-prometheus-rw /scripts/recommendation/03-generation-baseline.js
```

---

## 해석 표

| 사용자 수 | Java 전체 | FastAPI 호출 | Python DB | transaction | frequent itemset | rule | user pick | response | 생성 사용자 |
|----------:|----------:|-------------:|----------:|------------:|-----------------:|-----:|----------:|---------:|------------:|
| 120 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 |
| 300 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 |
| 500 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 |
| 1000 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 | 확인 |

