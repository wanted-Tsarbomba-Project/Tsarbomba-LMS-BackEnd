# 모니터링 컨벤션 & 레퍼런스 (팀 공용)

> 실행 순서는 [`PROCESS.md`](PROCESS.md). 이 문서는 **"지켜야 하는 규칙(컨벤션)" + "배운 일반 지식(레퍼런스)"**다.
> 컨벤션(§1~§3)은 **강제**다. 지금부터 안 지키면 나중에 팀 통합 대시보드가 불가능해진다.

---

# 1. 메트릭 네이밍 컨벤션 (강제)

## 1.1 전제 — 우리는 모놀리식 단일 앱

한 앱에 chat·order·submission·course… 가 같이 떠 있다. 그래서 도메인 구분 방식이 메트릭 종류에 따라 **두 갈래**로 나뉜다.

| 메트릭 종류 | 도메인 구분 방식 | 이유 |
|-------------|------------------|------|
| **커스텀 메트릭**(내가 직접 만드는 Counter/Timer) | **이름에 도메인 prefix** | 이름을 내가 정할 수 있으니 prefix로 네임스페이스 |
| **Spring 기본 메트릭**(HTTP·JVM·Hikari 등 자동 생성) | **`domain` 태그** (Phase 0 `DomainObservationConvention`이 자동 부착) | 이름을 못 바꿈. 핸들러 패키지로 도메인 판별 |

> **`management.metrics.tags.application` 공통 태그로 도메인을 못 가른다.** 단일 앱이라 값이 하나뿐 → 전 도메인이 같은 값으로 오염. 그래서 `domain` 태그(패키지 기반)가 따로 필요하다.

## 1.2 커스텀 메트릭 이름 규칙

```text
<domain>_<area>_<thing>_<unit>
```

| 종류 | suffix | 예시 |
|------|--------|------|
| Counter | `_total` | `chat_message_send_total`, `order_create_failed_total` |
| Timer | `_seconds` (자동 `_sum`/`_count`/`_max`) | `chat_message_send_duration_seconds` |
| Gauge | (단위) | `chat_active_streams` |

- `<domain>` = 패키지명과 일치(`chat`, `order`, `submission`, `course`, `problems`, `learning` …)
- Micrometer는 `.`을 `_`로 변환한다. 코드에서 `chat.message.send`로 등록하면 Prometheus에선 `chat_message_send`. **혼선 방지를 위해 등록 시점부터 `_`로 통일 권장.**

## 1.3 `domain` 태그 (HTTP/시스템 메트릭)

Phase 0의 `DomainObservationConvention`(Boot 3 Observation API)이 모든 `http_server_requests` 메트릭에 핸들러 패키지 기준 `domain` 라벨을 자동으로 붙인다. `admin.operation.*` 같은 중첩 패키지는 첫 세그먼트 `admin` 하나로 묶는다. 팀원은 **추가 작업 없음**. 조회만:

```promql
http_server_requests_seconds_count{domain="chatbot"}
```

> URI 경로(`/api/v1/users`)는 도메인 경계로 못 쓴다 — user·course 도메인이 같은 경로를 공유하고, learning/enrollment/course/lecture는 베이스가 `/api/v1`라 도메인 세그먼트가 없다. 그래서 패키지 기반 `domain` 태그가 정답.

---

# 2. 구조화 로깅 컨벤션 (강제, Loki 사용 전제)

Loki 쿼리(`event=`, `durationMs=`, `traceId=` 추출)가 먹히려면 **전 도메인이 같은 로그 형식**을 써야 한다.

> **우리 로그는 JSON이다** (`logback-spring.xml`의 `LogstashEncoder`, `logs/lms-app.log`). 그래서:
> - `event=...`·`durationMs=...`는 JSON의 `message` 필드 안 문자열로 들어간다 → Loki **line filter `|= "event=..."`** 는 raw JSON 라인을 매칭하므로 그대로 먹는다. `durationMs` 숫자 추출은 `| regexp` 로(아래 §8).
> - `traceId`는 MDC라서 **JSON 최상위 필드**로 나간다 → `| json` 으로 추출 가능.
> - promtail이 `level`·`log_type`을 JSON에서 뽑아 **Loki 라벨**로 만든다(`{job="lms", level="ERROR"}`). 이미 `monitoring/promtail/promtail-config.yml`에 적용됨 — 평문으로 되돌리지 말 것.

## 2.1 이벤트 로그 형식

```text
event=<domain>_<verb>  <key>=<value> ...  durationMs=<n>  traceId=<id>
```

예:
```text
event=chat_message_sent roomId=12 resultCount=1 durationMs=842 traceId=a1b2c3d4
event=order_create_failed reason=insufficient_stock exceptionType=InsufficientStockException traceId=...
```

규칙:
- `event=<domain>_<verb>` — 도메인 prefix + 과거형 동사
- 소요시간은 **`durationMs=`** 키로(Loki regexp 추출용)
- `traceId`는 MDC에서 자동(이미 `MdcLoggingFilter` 존재)
- **`traceId`·`durationMs`는 Loki label로 만들지 않는다** — 카디널리티 폭발. 로그 본문에서 추출(regexp/unwrap).

## 2.2 공통 요청 완료 로그 (Phase 0)

`MdcLoggingFilter`가 응답 직전 1줄 남긴다(팀원 추가 작업 없음):
```text
event=request_completed uri=/api/v1/chat/list method=GET status=200 durationMs=37 traceId=...
```
→ Loki "느린 요청" 패널이 이걸 본다.

---

# 3. 산출물 위치 컨벤션 (강제)

| 산출물 | 위치 |
|--------|------|
| 도메인별 모니터링 산출물(문서) | `monitoring/docs/domains/<domain>/` |
| k6 공통 lib | `monitoring/k6/lib/` |
| k6 도메인 시나리오 | `monitoring/k6/scripts/<domain>/*.js` |
| k6 결과 | `monitoring/k6/results/` |

> k6는 도커 스택(`monitoring/docker-compose.yml`)의 `k6` 컨테이너로 실행한다. `lib`/`scripts`/`results`가 그대로 컨테이너에 마운트된다. 실행법은 `monitoring/README.md`.

상세 파일 목록은 [`PROCESS.md` §Definition of Done](PROCESS.md).

---

# 4. 레퍼런스 — k6 핵심 개념

| 개념 | 설명 |
|------|------|
| VU | 동시에 행동하는 가상 사용자 |
| iteration | VU가 함수를 1회 실행한 횟수 |
| scenario / executor | 실행 모델·부하 패턴 (`ramping-vus`, `constant-arrival-rate` 등) |
| check | 개별 응답이 기대 조건을 만족하는지 검증 |
| threshold | 테스트 **전체**의 합격 기준 |
| sleep | 실제 사용자처럼 요청 사이 대기 |
| Trend/Rate/Counter | 커스텀 메트릭(분포/비율/누적) |

executor 선택:
- **`ramping-vus`** — 사용자 수를 늘리는 모델(점진 증가)
- **`constant-arrival-rate`** — 초당 요청률(RPS) 고정. 운영 목표가 RPS면 이쪽.

---

# 5. 레퍼런스 — p95/p99 해석

- **평균이 아니라 백분위수**를 본다. 평균은 일부 느린 요청을 숨긴다.
- `p95 = 500ms` → 요청 95%가 500ms 이하, 5%는 더 느림.
- **p95 = 기본 합격 기준 / p99 = tail latency 관찰 신호.** (로컬 짧은 테스트는 표본 적어 p99가 크게 흔들림 → threshold로 쓰지 않음)

| 상황 | 해석 |
|------|------|
| 평균 낮은데 p95 높음 | 일부 요청이 느려져 경험이 갈라짐 |
| p95·p99 함께 증가 | 전반적 처리 지연/병목 |
| p99만 튐 | GC·DB lock·커넥션 대기 등 outlier |
| p95 좋은데 실패율 높음 | 빠르게 실패 중 → 성공 아님 |

---

# 6. 레퍼런스 — 성공 기준 (API 유형별 기본값)

> 반드시 **트래픽 조건 + 측정 기간**과 함께 적는다. "평균 500ms 이하" 같은 기준은 금지.

| API 유형 | 기준 예시 |
|----------|-----------|
| 단순 조회 | `p95 < 300ms`, `p99 < 1s` |
| 집계/검색 | `p95 < 500~800ms`, `p99 < 2s` |
| 쓰기/주문/제출 | `p95 < 800ms~1s`, `p99 < 3s` |
| 외부 연동 | `p95 < 2s`, `p99 < 5s` |
| 비동기/스트리밍 | 응답시간보다 큐 적체·동시성 한계·타임아웃 동작 기준 |

공통 기본값(로컬):
```text
- http_req_failed < 1%
- http_req_duration p95 < 1000ms
- 도메인 핵심 API는 위 유형표 기준 별도 적용
```

k6 threshold 표현:
```javascript
export const options = {
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{type:list}': ['p(95)<500'],
    'http_req_duration{type:stream}': ['p(95)<3000'],
  },
};
```

원칙:
- 평균 아닌 p95를 합격 기준으로.
- 실패율 높으면 latency 낮아도 실패.
- **비즈니스 실패(재고부족 400 등)와 시스템 장애(5xx)를 분리**해서 해석.

---

# 7. 레퍼런스 — PromQL (부하 중 볼 지표)

도메인 필터는 `{domain="<도메인>"}` 추가.

```promql
# 전체/URI별 RPS
sum(rate(http_server_requests_seconds_count[1m]))
sum by (uri) (rate(http_server_requests_seconds_count{domain="chatbot"}[1m]))

# URI별 평균 응답시간 (k6의 p95와 다름 — 이건 평균, k6는 클라이언트 percentile)
sum by (uri) (rate(http_server_requests_seconds_sum{domain="chatbot"}[1m]))
/ sum by (uri) (rate(http_server_requests_seconds_count{domain="chatbot"}[1m]))

# 오류율
sum(rate(http_server_requests_seconds_count{status=~"4..|5.."}[1m]))
/ sum(rate(http_server_requests_seconds_count[1m]))

# 커스텀 timer 평균 (예: 챗봇)
rate(chat_message_send_duration_seconds_sum[1m])
/ rate(chat_message_send_duration_seconds_count[1m])

# 리소스
hikaricp_connections_active
process_cpu_usage
sum(jvm_memory_used_bytes{area="heap"})
```

---

# 8. 레퍼런스 — LogQL (부하 중 볼 로그)

> job 라벨은 **`lms`** 다(promtail `monitoring/promtail/promtail-config.yml`). 로그는 JSON이라 line filter는 raw 라인에 먹고, 숫자/필드 추출은 `regexp`/`json`을 쓴다.

```logql
# 도메인 이벤트 (line filter는 JSON message 안 문자열도 매칭)
{job="lms"} |= "event=chat_message_sent"

# 에러만 (promtail이 뽑은 level 라벨)
{job="lms", level="ERROR"}

# 느린 요청 (500ms+)
{job="lms"}
  |= "event=request_completed"
  != "uri=/actuator/prometheus"
  | regexp "durationMs=([5-9][0-9]{2}|[1-9][0-9]{3,})"

# durationMs 평균 추출
avg_over_time(
  {job="lms"} |= "event=chat_message_sent"
    | regexp "durationMs=(?P<durationMs>[0-9]+)" | unwrap durationMs [1m]
)

# traceId로 한 요청 전체 추적 (traceId는 JSON 필드)
{job="lms"} | json | traceId="복사한-id"
```

---

# 9. 레퍼런스 — Grafana 도메인 대시보드 행 구성

공통 템플릿(`domain` 변수 1개로 갈아끼움)은 아래 행으로 구성한다.

| Row | 목적 | 패널 |
|-----|------|------|
| Traffic | 부하가 의도대로 들어왔나 | Total RPS, RPS by URI, 커스텀 호출수 |
| Latency | 어디가 느려졌나 | URI별 평균/최대 응답시간, 커스텀 query duration |
| Error | 실패 증가했나 | HTTP Error Rate, status별 count, 실패 사유별 |
| Resource | 서버/DB 병목 힌트 | JVM Heap, Process CPU, Hikari Active |
| Logs | 수치 원인 추적 | 도메인 이벤트 로그, 느린 요청 로그, 에러 로그 |

해석 순서:
1. k6에서 p95·실패율 확인 →
2. Grafana RPS가 의도만큼 들어왔나 →
3. 느려진 URI 찾기 →
4. 커스텀 metric도 같이 증가했나 →
5. Loki로 durationMs·traceId 추적 →
6. 오류면 event 로그 확인 →
7. 쿼리/인덱스/동시성 가설 정리.

> **우리 스택은 k6 메트릭도 Prometheus로 보낸다** — k6 컨테이너를 `-o experimental-prometheus-rw`로 실행(`monitoring/docker-compose.yml`, Prometheus `--web.enable-remote-write-receiver`). 그래서 교안과 달리 **Grafana에서 k6 클라이언트 p95·실패율(`k6_http_req_duration_*` 등)까지** 서버측 메트릭과 같은 화면에서 본다.
> 단 k6 콘솔·`monitoring/k6/results/*.md` 요약도 그대로 남으니 전후 비교는 그 파일로 한다.

---

# 10. 레퍼런스 — 해석 시나리오 (교안 §14)

| 관찰 | 해석 |
|------|------|
| 핵심 API p95만↑ + 커스텀 query duration↑ + Loki durationMs↑ | 그 쿼리(집계/N+1)가 병목 |
| 여러 API 동시↓ + Hikari active 높게 유지 | DB 커넥션/트랜잭션/락 경합 |
| 실패율↑ + `reason=validation`↑ | 시스템 장애 아님 — 테스트 데이터/요청 body 오류 |
| 외부연동 API만 느림 + CPU 낮음 | 외부 응답 지연·커넥션풀 대기 (내부 최적화 대상 아님) |

---

## 참고
- 실행 프로세스: [`PROCESS.md`](PROCESS.md)
- 원본 교안: `K6.md` (Stage 4) — 쇼핑몰 예시. 개념·쿼리의 원천이나 병목 유형은 도메인마다 다름에 주의.
- 응답/에러 컨벤션(별개): [`../CONVENTION.md`](../../docs/CONVENTION.md)
