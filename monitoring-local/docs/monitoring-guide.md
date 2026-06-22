# Monitoring Guide (CodeBomb LMS)

이 문서는 **"어떤 상황에 어떤 모니터링 기술을 쓰는가"**를 기준별로 정리한 레퍼런스다.
수업(Actuator·Micrometer·Prometheus·Grafana)에 더해, 우리 프로젝트가 실제로 쓰는 **Loki 로그·모놀리식 domain 태그**까지 우리 코드에 연결해 설명한다.

> - 지켜야 하는 **규칙** → [`CONVENTION.md`](CONVENTION.md) (단일 소스)
> - **실행 순서**(7단계) → [`domains/README.md`](domains/README.md)
> - 메트릭/로그를 **스스로 설계** → [`domains/metrics-and-loki-guide.md`](domains/metrics-and-loki-guide.md)
> - 도구 켜고/끄기 → [`../README.md`](../README.md)

---

## 1. 한 줄 요약

```
Spring Boot App
  -> Actuator 가 운영 endpoint(/actuator/*)를 연다
  -> Micrometer 가 metric 을 기록한다 (HTTP 자동 + 커스텀)
  -> Prometheus 가 /actuator/prometheus 를 5초마다 수집한다
  -> Grafana 가 Prometheus 를 그래프로 보여준다
  +  MdcLoggingFilter 가 JSON 로그를 남기고 -> Promtail -> Loki 로 모인다
  +  Grafana 가 Loki 로그도 같은 화면에서 보여준다
```

| 기술 | 비유 | 역할 | 우리 프로젝트에서 |
|---|---|---|---|
| Actuator | 관리실 창문 | 앱 상태를 HTTP로 노출 | `/actuator/health`, `/actuator/prometheus` |
| Micrometer | 계기판 | 주문/오류/시간을 기록 | HTTP 자동 + 커스텀(`chat_room_list_query_duration`) |
| MeterRegistry | 계기판 장부 | Counter/Timer 등록 | `ChatMetrics` 생성자에서 주입 |
| Prometheus | 기록 창고 | 시간순 metric 저장 | job `lms-app`, 5초 scrape |
| Grafana | 그래프 TV | 대시보드/알림 | "LMS 도메인 대시보드"(domain 변수) |
| HealthIndicator | 건강검진 항목 | 구성요소 상태 확인 | (권장: FastAPI·MySQL·Redis — §11) |
| Loki + Promtail | 사건 일기장 + 배달부 | 요청별 상세 로그 | `event=...`, `traceId` (§9) |
| Log/traceId | 일기 한 줄 | 그 요청에 무슨 일이 | `MdcLoggingFilter` |

> **교안과 가장 큰 차이**: 우리는 **모놀리식 단일 앱**이고 **Loki까지 쓴다.** 그래서 (1) 도메인 구분이 메트릭 종류별로 두 갈래(§2.3) (2) "왜 느린가"를 Loki 로그로 좁힌다(§9).

---

## 2. 현재 프로젝트에 들어간 모니터링 설정

### build.gradle
```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
runtimeOnly 'io.micrometer:micrometer-registry-prometheus'
implementation 'net.logstash.logback:logstash-logback-encoder:7.4'   // JSON 로그(Loki용)
```
| 의존성 | 의미 |
|---|---|
| `spring-boot-starter-actuator` | `/actuator/*` 운영 endpoint 생성 |
| `micrometer-registry-prometheus` | metric을 Prometheus 형식으로 노출 |
| `logstash-logback-encoder` | 로그를 JSON으로 → Loki가 `traceId` 등을 필드로 파싱 |

### application.yml — 노출 endpoint
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus
```
| endpoint | 목적 |
|---|---|
| `/actuator/health` | 앱 생존 확인 |
| `/actuator/info` | 앱 정보 |
| `/actuator/prometheus` | Prometheus 수집용 metric |

> 교안과 달리 우리는 `metrics`·커스텀 endpoint를 노출하지 않는다(필요 최소). ⚠️ `exposure.include`는 보안이 아니다 — 우리는 Spring Security를 쓰므로 운영에선 actuator 경로 접근 제어를 따로 확인해야 한다(§11).

### 도메인 태그 (모놀리식 핵심)
`DomainObservationConvention`이 모든 `http_server_requests` 메트릭에 **컨트롤러 패키지 기준 `domain` 태그**를 자동 부착한다. → 도메인별 대시보드가 이 태그 하나로 가능.

### 스택 (monitoring/docker-compose.yml)
Prometheus + Grafana + Loki + Promtail + loadtest MySQL(3307) + k6. 실행법은 [`../README.md`](../README.md).

---

## 3. 기술별 사용 기준 (요약표)

| 상황 | 기술 | 이유 |
|---|---|---|
| 앱이 살아 있는지 | Actuator Health | 가장 단순한 생존 확인 |
| DB/Redis/외부 API 상태 | HealthIndicator | 구성요소별 정상 여부 |
| 어느 도메인/URI가 느린가 | HTTP 메트릭 + `domain` 태그 | 자동 수집(추가 작업 0) |
| 사건이 몇 번 일어났나 | Micrometer Counter | 단조 증가 횟수 |
| 값의 크기/분포 | DistributionSummary | 금액·개수·길이 분포 |
| 어떤 작업이 얼마나 걸리나 | Timer | 실행 횟수 + 시간 동시 |
| 지금 이 순간 값(오르내림) | Gauge | 동시 스트림·활성 세션 |
| metric을 오래 저장 | Prometheus | 재시작·기간 비교 |
| 그래프로 보기 | Grafana | 대시보드/알림 |
| 왜 그랬는지 원인 | Loki 로그 + traceId | 그 요청의 상세 흐름 |

---

## 4. Actuator를 써야 하는 기준

운영 정보를 HTTP로 확인할 때.

```
GET /actuator/health   → {"status":"UP"}
```
- 로드밸런서/오케스트레이터가 트래픽·재시작 판단할 때
- 운영자가 빠르게 상태 확인할 때

`/actuator/prometheus`는 Prometheus가 긁어가는 metric 원본이다(사람이 직접 볼 일은 디버깅 때뿐). **장기 저장·그래프는 Prometheus+Grafana**가 담당한다.

> 교안의 `/actuator/shop` 같은 custom endpoint는 "지금 이 순간 상태"만 보여주고 시간축이 없다. 우리는 안 만들었고, 시간 흐름이 필요하면 Prometheus metric을 쓴다. "현재 카운트만 즉시" 같은 특수 요구가 생기면 그때 custom endpoint를 검토.

---

## 5. Micrometer를 써야 하는 기준

코드 안에서 의미 있는 숫자를 직접 기록할 때. 우리 예시 클래스:
```
src/main/java/com/wanted/codebombalms/chatbot/infrastructure/metrics/ChatMetrics.java
```

> ⚠️ 네이밍은 [`CONVENTION.md`](CONVENTION.md)가 단일 소스. 커스텀은 **이름에 `<도메인>_` prefix**, HTTP/시스템은 **`domain` 태그**.

### Counter — "몇 번 일어났나" (단조 증가)
LMS에서 쓸 곳: 로그인 성공/실패 수, 챗봇 메시지 전송 수, 제출 횟수, 수강신청 횟수, API 오류 수.
```java
// 예: order_create_failed_total{reason="..."}
```
쓰면 안 되는 곳: 재고/접속자처럼 오르내리는 값 → **Gauge**.

### Timer — "얼마나 오래 걸리나"
LMS에서 쓸 곳: 챗봇 목록 쿼리 시간(현재 적용됨), 제출 채점 시간, 랭킹 집계 쿼리 시간, 외부 AI(FastAPI) 호출 시간.

현재 적용된 Timer:
| metric (Prometheus) | 위치 | 의미 |
|---|---|---|
| `chat_room_list_query_duration_seconds_*` | `ChatRoomQueryService.listRooms` | 채팅방 목록 조회 구간(N+1 fanout 포함) |

Timer가 답하는 질문: 몇 번 실행? 총 시간? 평균? 최대?

### DistributionSummary — "값이 얼마나 큰가" (시간 아님)
LMS에서 쓸 곳: 제출 코드 길이, 한 수강신청의 과목 수, 챗봇 응답 토큰 수, 업로드 파일 크기.

Timer와 구분:
| 질문 | 기술 |
|---|---|
| 얼마나 오래 걸렸나? | Timer |
| 값이 얼마나 큰가? | DistributionSummary |

### Gauge — "지금 이 순간"
LMS에서 쓸 곳: **동시 SSE 스트림 수**(챗봇 트랙 B `chat_active_streams`), 현재 활성 세션 수.

---

## 6. Prometheus를 써야 하는 기준

metric을 시간 순서로 저장할 때. 앱은 재시작하면 메모리 metric이 사라지지만, Prometheus는 `/actuator/prometheus`를 주기 수집해 보존한다.

| 상황 | 필요도 |
|---|---|
| 지금 값만 확인 | 낮음 |
| 1시간 전 vs 지금 비교 | 높음 |
| 재시작 후 과거 metric | 높음 |
| 부하 전/후 비교 | 높음 |
| 알림 조건 | 높음 |

### 이름 변환 (Micrometer → Prometheus)
| 등록 이름 | Prometheus 노출 |
|---|---|
| `chat_room_list_query_duration` (Timer) | `..._seconds_count`, `..._seconds_sum`, `..._seconds_max` |
| (Counter `xxx`) | `xxx_total` |
| `http.server.requests` (자동) | `http_server_requests_seconds_*{domain="...",uri="...",status="..."}` |

⚠️ Timer 등록명에 `_seconds`를 직접 붙이지 말 것(자동 부착됨). 정확한 이름은 `/actuator/prometheus`에서 확인이 가장 안전.

### scrape 설정 (우리 실제, monitoring/prometheus/prometheus.yml)
```yaml
scrape_configs:
  - job_name: "lms-app"
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ["host.docker.internal:8080"]   # 도커 Prometheus → 호스트 앱
```
> 우리는 scrape 5초(로컬 실습용). k6도 `-o experimental-prometheus-rw`로 자기 클라이언트 metric(`k6_*`)을 Prometheus에 보낸다 → Grafana에서 서버측+클라이언트측을 같이 본다.

---

## 7. Grafana를 써야 하는 기준

Prometheus/Loki 숫자·로그를 사람이 보기 좋게.

| 상황 | 필요도 |
|---|---|
| metric 이름만 확인 | 낮음 |
| 실시간 상황 한 화면 | 높음 |
| 장애 당시 그래프로 원인 추적 | 높음 |
| 팀 공유 | 높음 |

우리 대시보드: **"LMS 도메인 대시보드"**(자동 프로비저닝). 상단 **`domain` 변수**로 도메인을 갈아끼운다. 행 구성: Traffic / Latency / Resource / Logs.

추천 패널(도메인 변수 `$domain` 적용):
| 패널 | PromQL |
|---|---|
| URI별 RPS | `sum by (uri)(rate(http_server_requests_seconds_count{domain="$domain"}[1m]))` |
| 에러율 | `sum(rate(http_server_requests_seconds_count{domain="$domain",status=~"4..\|5.."}[1m])) / sum(rate(http_server_requests_seconds_count{domain="$domain"}[1m]))` |
| URI별 평균 응답시간 | `sum by (uri)(rate(http_server_requests_seconds_sum{domain="$domain"}[1m])) / sum by (uri)(rate(http_server_requests_seconds_count{domain="$domain"}[1m]))` |
| 커스텀 쿼리 평균(chat) | `rate(chat_room_list_query_duration_seconds_sum[1m]) / rate(chat_room_list_query_duration_seconds_count[1m])` |

시스템 패널(앱 전체, 도메인 무관):
| 패널 | metric |
|---|---|
| JVM Heap | `jvm_memory_used_bytes{area="heap"}` |
| CPU | `process_cpu_usage` |
| Hikari 커넥션 | `hikaricp_connections_active` |
| scrape 생존 | `up{job="lms-app"}` |

---

## 8. 도메인 서비스에 모니터링 붙이는 기준

교안은 OrderService/PaymentService 등 shop 파일별로 설명했다. 우리는 **도메인 서비스마다** 아래 기준으로 붙인다(파일럿: 챗봇).

| 서비스 성격 | 관찰 대상 | 기술 |
|---|---|---|
| **조회/집계** (챗봇 `/list`, 랭킹, 통계) | 조회 구간 시간, 호출 수 | Timer + (선택)Counter. HTTP는 domain 태그로 자동 |
| **외부 연동/스트리밍** (챗봇 SSE→FastAPI, 메일) | 외부 호출 시간, 동시 진행 수, 실패 | Timer + Gauge + Counter(reason) + HealthIndicator |
| **쓰기/트랜잭션** (제출, 수강신청) | 처리 시간, 실패 사유, 값 분포 | Timer + Counter(reason) + DistributionSummary |
| **인증** (로그인) | 성공/실패 수, 처리 시간 | Counter + Timer |

### 파일럿 적용 예 — 챗봇 목록 조회
`ChatRoomQueryService.listRooms`: 목록 조회 구간을 Timer로 감싸고 `event=chat_room_list_queried durationMs=` 로그를 남긴다. → "HTTP p95 ↑ + 커스텀 timer ↑ + 로그 durationMs ↑"가 같이 움직이면 N+1 확정.

### 오류 메트릭의 tag 규칙 (중요)
실패를 셀 때 `reason` 같은 tag는 **값 종류가 제한적**이어야 한다.
```
좋은 tag:  reason="bad_request" / "validation" / "server_error"
나쁜 tag:  reason="userId=123 요청 실패"  (값이 무한 → 카디널리티 폭발)
```
userId·orderId·예외 메시지는 **메트릭 tag가 아니라 Loki 로그 본문**에 남긴다(§9).

---

## 9. Loki(로그)를 써야 하는 기준  ★우리만의 추가

메트릭이 "얼마나/추세"라면, 로그는 **"왜"**다. 메트릭으로 이상을 감지 → 로그로 원인을 좁힌다.

우리 로그 파이프라인:
```
앱이 JSON 로그를 logs/lms-*.log 에 씀 (logback LogstashEncoder)
  → Promtail 이 tail 해서 Loki 로 push (job="lms")
  → Grafana(Explore/패널)에서 LogQL 조회
```

공통 로그(자동, 모든 요청):
```
event=request_completed uri=/api/v1/chat/list method=GET status=200 durationMs=37 traceId=a1b2c3d4
```
도메인 로그(내가 남김):
```
event=chat_room_list_queried resultCount=200 durationMs=408
```

자주 쓰는 LogQL:
```logql
{job="lms"} |= "event=chat_room_list_queried"                       # 도메인 이벤트
{job="lms", level="ERROR"}                                          # 에러만(라벨)
{job="lms"} |= "event=request_completed" | regexp "durationMs=([5-9][0-9]{2}|[1-9][0-9]{3,})"   # 느린 요청 500ms+
{job="lms"} | json | traceId="복사한-id"                            # 한 요청 전체 추적
```

⚠️ **traceId·durationMs를 라벨로 만들지 말 것** — 값이 무한해 Loki가 스트림 폭발한다. 본문에 두고 `regexp`/`json`으로 추출. (자세히: [`domains/metrics-and-loki-guide.md`](domains/metrics-and-loki-guide.md))

---

## 10. 모니터링 설계 기준 (새 기능에 붙일 때)

```
현재 상태인가?        -> Actuator Health / HealthIndicator
어느 도메인이 느린가?  -> HTTP 메트릭 + domain 태그 (자동)
사건 횟수인가?        -> Counter        (+ Prometheus rate, Grafana)
처리 시간인가?        -> Timer
값의 크기인가?        -> DistributionSummary
지금 이 순간 값인가?   -> Gauge
시간순 저장하고 싶다?  -> Prometheus
그래프로?            -> Grafana
왜 그랬는지 원인?      -> Loki 로그 + traceId
```
metric tag에 userId·orderId·예외메시지를 넣지 않는다(→ Loki).

---

## 11. 알림 기준 예시 (Grafana Alert / Alertmanager)

| 상황 | 기준 예시 |
|---|---|
| 서버 죽음 | `up{job="lms-app"} == 0` |
| 도메인 5xx 발생 | `sum(rate(http_server_requests_seconds_count{domain="chatbot",status=~"5.."}[5m])) > 0` |
| 응답 지연 증가 | `... http_server_requests_seconds_sum / ..._count > 1` (도메인·URI별) |
| 챗봇 목록 쿼리 지연 | `rate(chat_room_list_query_duration_seconds_sum[5m]) / rate(chat_room_list_query_duration_seconds_count[5m]) > 0.5` |
| JVM heap 높음 | heap 사용률 80%+ |
| DB 커넥션 부족 | `hikaricp_connections_active`가 max에 근접 |

로컬 실습은 임계값을 낮게, 운영은 평소 트래픽 기준으로 조정.

---

## 12. 현재 코드에서 개선하면 좋은 부분 (우리 기준)

1. **도메인별 커스텀 메트릭 확대** — 지금은 챗봇 `chat_room_list_query_duration` 하나뿐. 각 도메인이 §8 기준으로 자기 핵심 구간에 Timer/Counter를 심어야 도메인 대시보드가 의미 있어진다.
2. **HealthIndicator 추가** — 현재 없음. 권장 대상:
   | 대상 | 이유 |
   |---|---|
   | FastAPI(챗봇 AI) | 외부 AI 죽으면 챗봇 핵심 기능 실패 |
   | MySQL | DB 연결이 서비스 핵심 |
   | Redis | 세션/캐시 의존 시 |
3. **`/actuator/info` 채우기** — git commit·빌드시간·버전 넣으면 운영 버전 확인에 유용.
4. **actuator 접근 제어** — `prometheus`는 수집 서버만, `health`는 제한 공개 등. 우리는 Spring Security가 있으니 actuator 경로 정책을 점검.
5. **분산 traceId(v2)** — FastAPI 붙으면 자체 MDC traceId로는 cross-service 추적 불가 → `micrometer-tracing`(W3C traceparent) 검토. (`domains/README.md` v2 백로그)

---

## 최종 판단 한 장

```
정상인지        -> health / HealthIndicator
어디가 느린가    -> domain 태그 HTTP 메트릭
몇 번           -> Counter
얼마나 오래      -> Timer
값이 큰가        -> DistributionSummary
지금 이 순간     -> Gauge
오래 저장        -> Prometheus
그래프          -> Grafana
왜              -> Loki + traceId
```

대시보드 기본 4질문:
1. 핵심 비즈니스 이벤트가 정상 발생하나? (도메인마다 다름 — 챗봇=메시지/조회, 제출=채점, 수강=등록)
2. 실패/오류가 평소보다 느나?
3. 사용자가 기다리는 핵심 구간이 느려지나?
4. 런타임·DB·HTTP가 안정적인가?
