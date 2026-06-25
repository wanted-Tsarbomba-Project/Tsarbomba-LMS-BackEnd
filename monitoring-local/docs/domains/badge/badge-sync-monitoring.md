# Badge Sync Monitoring

배지 자동 획득 동기화 API의 병목을 로컬 모니터링 스택에서 확인하기 위한 문서입니다.

대상 API:

```text
POST /api/v1/badges/me/sync
```

---

## 목적

배지 동기화는 사용자의 총 포인트를 기준으로 획득 가능한 배지를 한 번에 여러 개 저장할 수 있는 쓰기성 API입니다.

동시 요청이 들어오면 다음 구간에서 병목이 생길 수 있습니다.

| 구간 | 확인 지표 |
|------|-----------|
| 획득 가능 배지 조회 | `badge_sync_grantable_lookup_duration` |
| 사용자가 이미 가진 배지 조회 | `badge_sync_earned_lookup_duration` |
| 신규 배지 저장 | `badge_sync_save_duration` |
| 전체 동기화 처리 | `badge_sync_duration` |
| DB 커넥션 압박 | `hikaricp_connections_active`, `hikaricp_connections_pending` |

---

## 실행 전 준비

Spring Boot 서버는 반드시 `loadtest` 프로필로 실행합니다.

```powershell
cd C:\Lecture\wan\Module03-project\Tsarbomba-Backend-module03-LMS
.\gradlew.bat bootRun --args='--spring.profiles.active=loadtest'
```

모니터링 스택은 `monitoring-local`에서 실행합니다.

```powershell
cd C:\Lecture\wan\Module03-project\Tsarbomba-Backend-module03-LMS\monitoring-local
docker compose up -d
```

---

## k6 실행

### Before 측정

```powershell
cd C:\Lecture\wan\Module03-project\Tsarbomba-Backend-module03-LMS\monitoring-local
docker compose run --rm `
  -e K6_PROMETHEUS_RW_SERVER_URL=http://host.docker.internal:9090/api/v1/write `
  -e K6_PROMETHEUS_RW_TREND_STATS="p(99),avg,min,med,max" `
  -e RESULT_NAME=badge-sync-before-optimization `
  k6 run -o experimental-prometheus-rw /scripts/badge/02-badge-sync-once-baseline.js
```

### After 측정

```powershell
cd C:\Lecture\wan\Module03-project\Tsarbomba-Backend-module03-LMS\monitoring-local
docker compose run --rm `
  -e K6_PROMETHEUS_RW_SERVER_URL=http://host.docker.internal:9090/api/v1/write `
  -e K6_PROMETHEUS_RW_TREND_STATS="p(99),avg,min,med,max" `
  -e RESULT_NAME=badge-sync-after-multi-row-insert `
  k6 run -o experimental-prometheus-rw /scripts/badge/02-badge-sync-once-baseline.js
```

주의:

| 항목 | 설명 |
|------|------|
| 재실행 조건 | 같은 사용자가 이미 배지를 획득하면 `newlyEarnedBadgeCount`가 0이 될 수 있습니다. 서버를 재시작하면 loadtest seeder가 테스트 데이터를 다시 정리합니다. |
| 결과 파일 | `monitoring-local/k6/results/*-summary.md`, `monitoring-local/k6/results/*-summary.json` |
| 주요 기준 | `http_req_duration{type:badge_sync_once}` p95, p99와 Hikari pending을 함께 확인합니다. |

---

## Prometheus 확인 쿼리

전체 동기화 평균:

```promql
rate(badge_sync_duration_seconds_sum[1m])
/
rate(badge_sync_duration_seconds_count[1m])
```

획득 가능 배지 조회:

```promql
rate(badge_sync_grantable_lookup_duration_seconds_sum[1m])
/
rate(badge_sync_grantable_lookup_duration_seconds_count[1m])
```

이미 획득한 배지 조회:

```promql
rate(badge_sync_earned_lookup_duration_seconds_sum[1m])
/
rate(badge_sync_earned_lookup_duration_seconds_count[1m])
```

신규 배지 저장:

```promql
rate(badge_sync_save_duration_seconds_sum[1m])
/
rate(badge_sync_save_duration_seconds_count[1m])
```

Hikari Pool:

```promql
hikaricp_connections_active
```

```promql
hikaricp_connections_pending
```

Spring HTTP 평균 응답 시간:

```promql
rate(http_server_requests_seconds_sum{uri="/api/v1/badges/me/sync"}[1m])
/
rate(http_server_requests_seconds_count{uri="/api/v1/badges/me/sync"}[1m])
```

k6 커스텀 메트릭 확인:

```promql
{__name__=~"k6_.*badge_sync.*"}
```

---

## Loki 확인 쿼리

배지 동기화 완료 로그:

```logql
{job="lms"} |= "event=badge_sync_completed"
```

배지 동기화 관련 에러:

```logql
{job="lms"} |= "badge" |= "ERROR"
```

로그에서 특히 볼 값:

| 로그 필드 | 의미 |
|-----------|------|
| `grantableLookupMs` | 획득 가능 배지 조회 시간 |
| `earnedLookupMs` | 이미 획득한 배지 조회 시간 |
| `saveMs` | 신규 배지 저장 시간 |
| `durationMs` | 전체 동기화 처리 시간 |
| `newlyEarnedBadgeCount` | 이번 요청에서 새로 저장된 배지 수 |

---

## Grafana 대시보드 해석

| 패널 | 해석 |
|------|------|
| `k6 Badge Sync p99` | k6 기준 badge sync 요청의 p99 응답 시간입니다. |
| `k6 Failed Rate` | k6 기준 실패율입니다. 0%가 정상입니다. |
| `Newly Earned Badge Count p99` | 테스트 요청에서 새로 획득한 배지 수입니다. 이번 시나리오에서는 100이 정상입니다. |
| `Badge Sync Valid Rate` | 응답 구조와 신규 배지 획득 조건을 만족한 비율입니다. 100%가 정상입니다. |
| `Badge Sync Internal Duration Avg` | 애플리케이션 내부 구간별 평균 처리 시간입니다. |
| `Spring HTTP Avg Duration - Badge Sync` | Spring MVC 기준 `/api/v1/badges/me/sync` 평균 응답 시간입니다. |
| `Hikari Pool Pressure` | DB 커넥션 active/pending 추이입니다. pending이 튀면 커넥션 대기 병목이 발생한 것입니다. |
| `Badge Sync Logs` | `event=badge_sync_completed` 구조화 로그입니다. |
| `Badge Sync Error Logs` | 배지 관련 에러 로그입니다. No data면 에러가 없다는 뜻입니다. |

---

## Before 결과

JPA `saveAll()` 기반 baseline:

```text
k6 summary: badge-sync-before-optimization
http_reqs        : 600
iterations       : 300
checks           : 100.00%
http_req_failed  : 0.00%

http_req_duration (ms)
  avg             : 536.16
  med             : 185.19
  p90             : 896.35
  p95             : 1552.94
  p99             : 1953.04
  max             : 1964.26
```

관찰 결과:

| 항목 | Before |
|------|--------|
| 신규 획득 배지 수 | 100 |
| Hikari pending | 약 40 |
| `saveMs` | 약 141ms |
| `durationMs` | 약 150ms |

판단:

```text
조회 구간은 짧지만 신규 배지 저장 구간에서 시간이 크게 발생했다.
동시 요청 상황에서 Hikari pending이 증가해 DB 커넥션 대기 병목이 확인됐다.
```

---

## 1차 개선 결과

JPA `saveAll()`을 JDBC `batchUpdate()`로 변경한 1차 개선:

```text
k6 summary: badge-sync-after-bulk-insert
http_reqs        : 600
iterations       : 300
checks           : 100.00%
http_req_failed  : 0.00%

http_req_duration (ms)
  avg             : 573.40
  med             : 314.93
  p90             : 818.69
  p95             : 1972.69
  p99             : 2371.16
  max             : 2391.59
```

관찰 결과:

| 항목 | Before | JDBC batch |
|------|--------|------------|
| `saveMs` | 약 141ms | 약 134ms |
| Hikari pending | 약 40 | 약 40 |
| p99 | 1953.04ms | 2371.16ms |

판단:

```text
JDBC batch 전환만으로는 저장 구간과 Hikari pending 병목이 충분히 개선되지 않았다.
MySQL 드라이버 설정에 따라 batch가 실제 multi-row insert로 재작성되지 않을 수 있으므로, 명시적인 multi-row insert 방식이 필요하다고 판단했다.
```

---

## 2차 개선 결과

한 사용자의 신규 배지를 하나의 multi-row `insert ignore` SQL로 저장하도록 변경:

```text
k6 summary: badge-sync-after-multi-row-insert
http_reqs        : 600
iterations       : 300
checks           : 100.00%
http_req_failed  : 0.00%

http_req_duration (ms)
  avg             : 310.81
  med             : 153.07
  p90             : 485.07
  p95             : 1607.97
  p99             : 1785.12
  max             : 1956.72
```

관찰 결과:

| 항목 | Before | Multi-row insert | 개선율 |
|------|--------|------------------|--------|
| avg | 536.16ms | 310.81ms | 약 42.0% 개선 |
| med | 185.19ms | 153.07ms | 약 17.3% 개선 |
| p90 | 896.35ms | 485.07ms | 약 45.9% 개선 |
| p95 | 1552.94ms | 1607.97ms | 약 3.5% 악화 |
| p99 | 1953.04ms | 1785.12ms | 약 8.6% 개선 |
| failed rate | 0% | 0% | 동일 |
| checks | 100% | 100% | 동일 |

내부 처리 시간:

| 구간 | Before | Multi-row insert | 개선율 |
|------|--------|------------------|--------|
| `saveMs` | 약 141ms | 약 3ms | 약 97.9% 개선 |
| `durationMs` | 약 150ms | 약 18ms | 약 88.0% 개선 |
| Hikari pending | 약 40 | 거의 0 | 대폭 개선 |

판단:

```text
multi-row insert 적용 후 저장 구간 시간이 크게 줄었고, Hikari pending도 거의 사라졌다.
전체 k6 지표에서도 avg, med, p90, p99가 개선되어 DB 쓰기 병목 완화가 확인됐다.
p95 꼬리 지연은 일부 남아 있으나, 이번 최적화 목표였던 저장 구간과 커넥션 대기 병목은 해소됐다.
```

---

## 최종 결론

배지 동기화 API의 주요 병목은 조회가 아니라 신규 배지 저장 구간이었습니다.

초기 구현은 JPA `saveAll()`로 신규 `user_badge`를 저장했기 때문에, worst-case 상황에서 다량의 insert가 발생했고 Hikari 커넥션 풀이 압박을 받았습니다.

JDBC `batchUpdate()`로 1차 개선을 시도했지만 개선폭이 작았고, 최종적으로 명시적인 multi-row `insert ignore` SQL을 사용해 한 사용자의 신규 배지를 한 번의 SQL로 저장하도록 변경했습니다.

그 결과:

- `saveMs`: 약 141ms -> 약 3ms
- `durationMs`: 약 150ms -> 약 18ms
- Hikari pending: 약 40 -> 거의 0
- k6 p99: 1953.04ms -> 1785.12ms
- 실패율: 0% 유지
- 유효 응답률: 100% 유지

이번 개선은 쓰기 병목에 대해 캐시보다 저장 방식 개선이 더 적합하다는 것을 보여줍니다.
