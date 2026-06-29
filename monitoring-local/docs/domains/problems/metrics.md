# Problems 도메인 M4 메트릭 / 로그 설계

> 병목 가설: `monitoring-local/docs/domains/problems/bottleneck-hypothesis.md`
> 현재 M4 측정 대상은 Problems 하위 기능 중 Ranking, Badge 입니다.

---

## 1. 측정 대상

| 우선순위 | 대상 | API | 병목 가설 |
| --- | --- | --- | --- |
| 1 | Ranking | `GET /api/v1/rankings/points`, `GET /api/v1/rankings/weekly`, `GET /api/v1/rankings/points/me` | 점수 집계, 내 순위 계산, 랭킹 데이터 유무에 따라 응답 실패율이 증가할 수 있음 |
| 2 | Badge | `POST /api/v1/badges/me/sync`, `GET /api/v1/badges/me` | 포인트 기준 배지 동기화 시 사용자 보유 배지와 전체 배지 비교 비용이 커질 수 있음 |

---

## 2. k6 커스텀 메트릭

| metric name | type | 대상 | 의미 |
| --- | --- | --- | --- |
| `ranking_response_valid_rate` | Rate | Ranking | 랭킹 응답 구조가 기대한 형태인지 확인 |
| `ranking_result_count` | Trend | Ranking | 랭킹 목록 응답 개수 |
| `ranking_my_rank` | Trend | Ranking | 내 랭킹 순위 값 |
| `ranking_total_point` | Trend | Ranking | 내 누적 포인트 값 |
| `badge_response_valid_rate` | Rate | Badge | 배지 응답 구조가 기대한 형태인지 확인 |
| `badge_owned_count` | Trend | Badge | 사용자가 보유한 배지 개수 |
| `badge_newly_earned_count` | Trend | Badge | 동기화로 새로 획득한 배지 개수 |
| `badge_total_point` | Trend | Badge | 배지 동기화 기준 사용자 포인트 |

---

## 3. k6 시나리오 태그

| type tag | 의미 |
| --- | --- |
| `ranking_total` | 전체 포인트 랭킹 조회 |
| `ranking_weekly` | 주간 포인트 랭킹 조회 |
| `ranking_me` | 내 포인트 랭킹 조회 |
| `badge_sync` | 내 배지 동기화 |
| `badge_list` | 내 배지 목록 조회 |

Grafana에서는 k6 기본 메트릭을 `type` 태그로 나눠서 봅니다.

```promql
histogram_quantile(0.95, sum by (le, type) (rate(k6_http_req_duration_seconds_bucket[1m])))
```

```promql
sum by (type) (rate(k6_http_reqs_total[1m]))
```

```promql
sum by (type) (rate(k6_http_req_failed_total[1m]))
/
sum by (type) (rate(k6_http_reqs_total[1m]))
```

---

## 4. 로그 기준

| event keyword | 대상 | 의미 |
| --- | --- | --- |
| `rankings` | Ranking | 랭킹 API 요청, 실패 로그 추적 |
| `badges` | Badge | 배지 목록/동기화 API 요청, 실패 로그 추적 |
| `point_reward_task` | Reward Point | 포인트 지급 복구 스케줄러 동작 추적 |

Loki에서는 아래처럼 도메인 키워드 기준으로 확인합니다.

```logql
{service="code-bomba-lms"} |= "rankings"
```

```logql
{service="code-bomba-lms"} |= "badges"
```

```logql
{service="code-bomba-lms"} |= "point_reward_task"
```

---

## 5. 성공 기준

| 대상 | 기준 |
| --- | --- |
| Ranking | VU 50, 약 70초 기준 `http_req_duration{type=ranking_*}` p95 < 500ms, 실패율 < 1% |
| Badge | VU 50, 약 70초 기준 `http_req_duration{type=badge_*}` p95 < 500ms, 실패율 < 1% |

랭킹은 데이터가 없으면 404가 발생할 수 있으므로, loadtest DB에 랭킹용 포인트/히스토리 시드가 필요합니다.

---

## 6. 실행 명령

```powershell
cd C:\Lecture\wan\Module03-project\Tsarbomba-Backend-module03-LMS\monitoring
```

```powershell
docker compose run --rm -e RESULT_NAME=ranking-before k6 run -o experimental-prometheus-rw /scripts/ranking/01-ranking-baseline.js
```

```powershell
docker compose run --rm -e RESULT_NAME=badge-sync-before k6 run -o experimental-prometheus-rw /scripts/badge/01-badge-sync-baseline.js
```

---

## 7. 정리

- 예전 문제세트 진입/제출/Runner 전용 Micrometer 메트릭은 현재 측정 대상에서 제거했습니다.
- 현재 대시보드는 k6 외부 관찰 지표와 Ranking/Badge 비즈니스 지표를 중심으로 봅니다.
- Spring 내부 상세 지표가 필요하면 Ranking/Badge 전용 메트릭을 별도 추가합니다.
