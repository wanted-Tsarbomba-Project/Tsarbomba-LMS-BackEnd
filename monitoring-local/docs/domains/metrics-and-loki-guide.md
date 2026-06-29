# 메트릭 & Loki 컨벤션 — 왜 이렇게 쓰고, 나중에 어떻게 쓰이나

> 2단계(메트릭/로그 심기)에서 막히면 이 문서를 봅니다.
> 규칙을 외우지 말고 **"이 규칙이 나중에 어디서 쓰이는지"**를 이해하세요. 그래야 내 도메인에 맞게 스스로 설계할 수 있습니다.

---

## 0. 먼저 — 관측 3층 (이미 깔린 것 vs 내가 심을 것)

```
Layer 1 [공짜·자동]  http_server_requests + domain 태그   → "어느 API/도메인이 느린가"
Layer 2 [공짜·자동]  MdcLoggingFilter 로그 + traceId       → "느린 요청 하나를 추적"
Layer 3 [내가 심음]  커스텀 Timer/Counter/Gauge + event 로그 → "그 안의 어느 구간/이유가 느린가"  ← 2단계
```

Layer 1·2는 팀 공통으로 이미 동작합니다(추가 작업 없음). **여러분이 2단계에서 하는 건 Layer 3뿐**입니다.

**그런데 Layer 1·2가 있는데 왜 Layer 3가 또 필요할까?**

| 공통(1·2)이 못 보는 것 | 그래서 커스텀(3)이 필요 |
|---|---|
| `http_server_requests`는 컨트롤러~직렬화 **전체 시간**만 안다. 그 안의 DB 구간만의 비용은 모른다 | 의심 구간을 Timer로 감싸면 그 구간만 분리 측정 → "HTTP가 느린 건 이 구간 때문"을 증명 |
| async/스트리밍은 요청이 일찍 리턴돼 시간 측정이 부정확 | Gauge로 "지금 동시에 몇 개 진행 중"을 직접 카운트 |
| Spring은 status(4xx/5xx)만 안다. "왜 실패했는지"는 모른다 | Counter에 `reason` 태그 → 실패 이유별 집계 |

---

# 1. 메트릭 (Prometheus)

## 1.1 왜 "이름"이 생명인가 — 수집 모델부터

Prometheus는 **5초마다 앱의 `/actuator/prometheus`를 긁어갑니다(pull).** 중앙 등록소가 없어서, **앱이 노출하는 메트릭 이름 그 자체가 영구 식별자**입니다. 나중에 PromQL도, 대시보드도 **정확한 이름**으로 부릅니다.

```
내 코드:  Timer.builder("chat_room_list_query_duration")   ← 이름
   ↓ (Prometheus가 긁어감, Timer라 _seconds 자동 부착)
PromQL:   rate(chat_room_list_query_duration_seconds_sum[1m])
          / rate(chat_room_list_query_duration_seconds_count[1m])
대시보드:  위 쿼리를 패널에 박음
```

→ 그래서 이름을 제각각 지으면(`chatCnt`, `order.create`, `submissionCounter`) **나중에 팀 통합 대시보드에서 한 번에 못 묶습니다.** 이름 규칙이 통합의 전제입니다.

## 1.2 이름 규칙 (강제)

```
<도메인>_<영역>_<대상>_<단위>
```

| 종류 | 언제 쓰나 | suffix | 예시 |
|---|---|---|---|
| **Timer** | "이 구간이 얼마나 걸리나" (지연 측정) | `_seconds` (자동 `_sum`/`_count`/`_max`) | `chat_room_list_query_duration` → `..._seconds_*` |
| **Counter** | "몇 번 일어났나" (횟수, 단조 증가) | `_total` | `order_create_failed_total` |
| **Gauge** | "지금 이 순간 값이 얼마" (오르내림) | (단위) | `chat_active_streams` |

- `<도메인>` = 내 패키지명 첫 세그먼트 (`chat`은 `chatbot`이지만 커스텀 이름은 짧게 `chat_`도 OK — 일관만 하면 됨).
- ⚠️ **Timer 등록명에 `_seconds`를 직접 붙이지 마세요.** Timer면 Prometheus가 자동으로 붙입니다. `..._duration` 으로 등록하면 `..._duration_seconds_*`가 됩니다.
- 코드에서 `.`(점) 쓰면 Prometheus가 `_`로 바꿉니다. 혼선 막으려면 등록 때부터 `_`로.

## 1.3 도메인 구분 — 두 갈래 (모놀리식이라 중요)

한 앱에 모든 도메인이 같이 떠 있어서, 메트릭 종류에 따라 도메인 구분 방식이 다릅니다.

| 메트릭 | 도메인 구분 | 쿼리 예 |
|---|---|---|
| **HTTP/시스템**(자동 생성, 이름 못 바꿈) | **`domain` 태그** (패키지 기반, 자동 부착) | `http_server_requests_seconds_count{domain="chatbot"}` |
| **커스텀**(내가 만듦) | **이름 prefix** | `chat_room_list_query_duration_seconds_count` |

> `domain` 태그 값 = **컨트롤러의 패키지 첫 세그먼트**입니다. `...codebombalms.chatbot...` → `domain="chatbot"`, `...course...` → `domain="course"`. 내 도메인 태그값이 헷갈리면 Prometheus에서 `count by (domain) (http_server_requests_seconds_count)` 로 확인하세요.

## 1.4 그래서 내 도메인에 뭘 심을까 (스스로 결정)

1단계 가설에서 의심한 **"느릴 것 같은 구간"**을 고릅니다.

| 내 의심이... | 심을 것 |
|---|---|
| "이 조회/집계 쿼리가 느릴 듯" | 그 메서드를 **Timer**로 감싸기 (`<도메인>_xxx_query_duration`) |
| "외부 API/스트리밍 동시성이 한계일 듯" | **Gauge**로 동시 진행 수 (`<도메인>_active_xxx`) |
| "특정 실패가 많을 듯" | **Counter** + `reason` 태그 (`<도메인>_xxx_failed_total`) |

구현 패턴 (Timer 예):
```java
@Component
public class XxxMetrics {
    private final Timer queryTimer;
    public XxxMetrics(MeterRegistry registry) {
        this.queryTimer = Timer.builder("<도메인>_xxx_query_duration")
            .description("...").register(registry);
    }
    public void record(long nanos) { queryTimer.record(nanos, TimeUnit.NANOSECONDS); }
}
```
서비스에서:
```java
long s = System.nanoTime();
... 의심 구간 ...
metrics.record(System.nanoTime() - s);
```

> 실제 예시는 [`example-read-list.md`](example-read-list.md)의 `ChatMetrics`를 그대로 따라 하면 됩니다.

---

# 2. Loki (로그)

## 2.1 왜 "형식"이 생명인가 — 수집 모델부터

로그는 메트릭과 반대로 **push**입니다.
```
앱이 JSON 로그를 파일에 씀 (logs/lms-*.log)
   → Promtail이 그 파일을 읽어 Loki로 밀어넣음
   → Grafana에서 LogQL로 조회
```

Loki는 **라벨로 스트림을 찾고, 본문은 텍스트로 훑습니다.** 그래서 나중 LogQL이 이렇게 동작합니다:
```logql
{job="lms"} |= "event=chat_room_list_queried"        # 본문 문자열 매칭
  | regexp "durationMs=(?P<d>[0-9]+)" | unwrap d       # 본문에서 숫자 추출
{job="lms"} | json | traceId="a1b2c3d4"               # JSON 필드로 한 요청 추적
```

→ `event=`·`durationMs=` 형식이 도메인마다 다르면 **이 쿼리가 그 도메인만 안 먹습니다.** 형식 통일이 곧 재사용 가능한 쿼리입니다.

## 2.2 로그 형식 규칙 (강제)

```
event=<도메인>_<과거형동사>  <key>=<value> ...  durationMs=<n>
```
예:
```
event=chat_room_list_queried resultCount=200 durationMs=408
event=order_create_failed reason=insufficient_stock
```

규칙:
- `event=<도메인>_<동사>` — 도메인 prefix + 과거형 (Loki line filter `|= "event=..."` 로 잡음)
- 소요시간은 **`durationMs=`** 키 고정 (regexp 추출용)
- `traceId`는 **자동**으로 붙습니다 (MdcLoggingFilter가 MDC에 넣음 → JSON 필드). 직접 안 적어도 됨.

## 2.3 ★ traceId·durationMs를 라벨로 만들지 마세요 (카디널리티)

Loki/Prometheus는 **라벨 값 조합마다 별도 스트림/시계열**을 만듭니다.
- `level`(INFO/WARN/ERROR) → 스트림 5개. OK.
- `traceId`(요청마다 고유) → 100만 요청 = 스트림 100만 개 → **Loki 폭발/다운**.

그래서 traceId·durationMs는 **본문에 두고 쿼리할 때 regexp로 추출**합니다. 라벨 금지.

## 2.4 그래서 내 도메인에 어떤 로그를 남길까 (스스로 결정)

1단계에서 의심한 **핵심 동작**마다 완료 로그 한 줄:
```java
log.info("event=<도메인>_<동사> <핵심필드>={} durationMs={}", value, elapsedMs);
```
- 측정 메트릭과 **같은 시점·같은 값**으로 남기면, "메트릭 추세"와 "로그 개별 사건"이 서로 검증됩니다.
- "왜 실패했나"를 알아야 하면 실패 로그에 `reason=`, `exceptionType=` 같이.

> 공통 요청완료 로그(`event=request_completed uri= durationMs=`)는 이미 모든 요청에 자동으로 남습니다(MdcLoggingFilter). 내 도메인 고유 동작만 추가로 남기면 됩니다.

---

# 3. 나중에 이 모든 게 합쳐지는 그림 (6단계)

```
1. k6 결과: p95가 목표 초과?            → "느리다" 감지
2. Grafana(Prometheus): 어느 uri가 느림? domain=내도메인 으로 필터
3. 내 커스텀 Timer도 같이 치솟나?        → "이 내부 구간이 원인" 좁힘
4. Grafana(Loki): event=<도메인>_... 로그의 durationMs 확인 + traceId 복사
5. traceId로 그 요청 전체 로그 추적       → "왜" 확정 (예: SQL 401줄 = N+1)
```

이 5단계가 돌아가려면:
- 2번 = `domain` 태그 (자동)
- 3번 = **내가 심은 커스텀 메트릭** (1.4)
- 4·5번 = **내가 남긴 event 로그 + 자동 traceId** (2.4)

→ **2단계에서 메트릭·로그를 규칙대로 심어야, 6단계 해석이 가능해집니다.** 이게 컨벤션을 강제하는 진짜 이유입니다.

---

다음: 위를 처음부터 끝까지 한 도메인에 적용한 예시 → [`example-read-list.md`](example-read-list.md)
