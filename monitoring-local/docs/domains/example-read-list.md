# 예시 (조회 기준) — 채팅방 목록 `GET /api/v1/chat/list` 처음부터 끝까지

> **조회/집계형 도메인의 완성 본보기**입니다. 실제로 돌려서 나온 숫자를 그대로 적었습니다.
> 자기 도메인이 "목록 조회·통계·랭킹" 류면 이 흐름을 그대로 미러링하세요.
> 전체 프로세스는 [`README.md`](README.md), 메트릭/로그 설계는 [`metrics-and-loki-guide.md`](metrics-and-loki-guide.md).

이 예시가 보여주는 병목: **N+1** — 목록을 가져온 뒤 행마다 추가 조회가 또 나가는 패턴.

---

## 1단계 — 가설

`ChatRoomQueryService.toResult` 가 채팅방 1개당 `problemTitlePort` 로 제목을 **2번** 조회합니다.
→ 방 N개면 `findByUserId` 1번 + 2N번 = **1 + 2N 쿼리**. 방이 늘수록 선형으로 느려진다(가설).

| 대상 API | 가설 | 관찰 지표 | 성공 기준 |
|---|---|---|---|
| `GET /api/v1/chat/list` | N+1 (방당 제목 2회) | HTTP p95, 커스텀 쿼리 timer, Hikari active | p95 < 500ms, 실패율 < 1% (VU 50, 방 다수) |

---

## 2단계 — 메트릭/로그 심기

**커스텀 Timer** — 목록 조회 구간만 분리 측정 (`chatbot/infrastructure/metrics/ChatMetrics.java`):
```java
@Component
public class ChatMetrics {
    private final Timer listQueryTimer;
    public ChatMetrics(MeterRegistry registry) {
        this.listQueryTimer = Timer.builder("chat_room_list_query_duration")  // _seconds 자동
                .description("채팅방 목록 조회 구간 시간(제목 fanout 포함)")
                .register(registry);
    }
    public void recordListQuery(long elapsedNanos) {
        listQueryTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
}
```

**서비스 계측 + 로그** (`ChatRoomQueryService.listRooms`):
```java
long startedAt = System.nanoTime();
List<ChatRoomResult> rooms = chatRoomRepository.findByUserId(userId)
        .stream().map(this::toResult).collect(Collectors.toList());
long elapsedNanos = System.nanoTime() - startedAt;

chatMetrics.recordListQuery(elapsedNanos);                                  // 메트릭
log.info("event=chat_room_list_queried resultCount={} durationMs={}",      // 로그(Loki)
        rooms.size(), elapsedNanos / 1_000_000);
```
→ Prometheus 노출: `chat_room_list_query_duration_seconds_{count,sum,max}`. Loki: `event=chat_room_list_queried`.

---

## 3단계 — 성공 기준

```
VU 50, ramping 0→50, 약 70초.
GET /api/v1/chat/list (조회형): http_req_duration{type:list} p95 < 500ms, http_req_failed < 1%.
```

---

## 4단계 — k6 시나리오

`monitoring/k6/scripts/chat/01-list-baseline.js` (인증 필요 → `setup()`에서 로그인):
```javascript
import http from "k6/http";
import { check, sleep } from "k6";
import { BASE_URL, randomSleep } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

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
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() { return { token: login() }; }   // 부하 전 1회 로그인

export default function (data) {
    // ⚠️ k6 babel 은 스프레드(...) 미지원 → params 객체에 tags 를 붙인다
    const params = authCookies(data.token);
    params.tags = { type: "list", api: "GET /chat/list" };
    const res = http.get(`${BASE_URL}/api/v1/chat/list`, params);

    check(res, {
        "status is 200": (r) => r.status === 200,
        "has data array": (r) => Array.isArray(r.json("data")),
    });
    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("chat-list-baseline");
```

---

## 5단계 — 시드 + baseline

**왜 시드가 필요한가**: loadtest DB는 부팅마다 비워집니다. 방 5개로는 N+1이 안 보입니다 → 방을 대량으로.

`@Profile("loadtest")` 시더가 부팅 직후 자동 적재 (`ChatListLoadTestSeeder.java`):
- 로그인 계정 1 + problem_set 10 + problem 200 + **그 계정 채팅방 200개** (각 방에 problemSetId/problemId).
- → `/list` 1회 = 1 + 400 쿼리.

> 다른 도메인은 자기 데이터로 같은 패턴의 시더를 만들면 됩니다 (raw JdbcTemplate batch, `@Profile("loadtest")`, 이미 시드돼 있으면 skip).

시드 확인:
```bash
docker compose exec mysql mysql -ulms_loadtest -ploadtest lms_loadtest \
  -e "select count(*) from chat_room;"     # 200 이면 OK
```

baseline 실행:
```bash
MSYS_NO_PATHCONV=1 docker compose run --rm -e RESULT_NAME=chat-list-before \
  k6 run -o experimental-prometheus-rw /scripts/chat/01-list-baseline.js
```

**실제 결과**:
| 지표 | 값 |
|---|---|
| http_req_duration p95 | **1497ms** (목표 500ms 3배 초과 → 불합격) |
| http_req_waiting p95 | 1493ms (≈ duration → 서버/DB 바운드) |
| 커스텀 chat_room_list_query avg | **408ms** |
| http_req_failed | 0% (기능 정상, 느린 것뿐) |

---

## 6단계 — 분석 (메트릭 + 로그 교차)

Grafana **"LMS 도메인 대시보드"** → 도메인=`chatbot`:
- `RPS by URI`: `/api/v1/chat/list` 선이 부하 때 상승.
- `커스텀: chat 목록 쿼리 구간 avg`: **~0.4s** ← N+1 비용을 그래프로.
- `Hikari Active`: 동시부하 때 상승 (커넥션 경합).

Prometheus 직접 확인:
```promql
# 커스텀 쿼리 구간 평균
rate(chat_room_list_query_duration_seconds_sum[1m]) / rate(chat_room_list_query_duration_seconds_count[1m])
# 내 도메인 HTTP 평균
sum by (uri)(rate(http_server_requests_seconds_sum{domain="chatbot"}[1m]))
  / sum by (uri)(rate(http_server_requests_seconds_count{domain="chatbot"}[1m]))
```

Loki (Grafana Explore → Loki):
```logql
{job="lms"} |= "event=chat_room_list_queried"
  | regexp "durationMs=(?P<d>[0-9]+)" | unwrap d
```
loadtest는 SQL DEBUG가 켜져 있어, 느린 요청 traceId로 추적하면 **한 요청에 SQL 401줄**이 보입니다 → N+1 육안 확인.

**결론**: HTTP p95 1497ms = 쿼리 구간 408ms = SQL 401줄. **삼위일체로 N+1 확정.**

---

## 7단계 — 최적화 → 전후 비교

이 예시의 정석 해법은 **batch 조회** (방들의 id를 모아 `IN` 한 번 → `1+2N → 1+2`).
> 단 이 케이스는 `problems`(타 도메인) 수정이 필요해 파일럿에선 **보류**하고 변경 지점만 기록했습니다 (`monitoring/docs/domains/chat/compare.md`).

전후 비교 방법:
```bash
# 최적화 적용 후 같은 스크립트, 이름만 after 로
MSYS_NO_PATHCONV=1 docker compose run --rm -e RESULT_NAME=chat-list-after \
  k6 run -o experimental-prometheus-rw /scripts/chat/01-list-baseline.js
```
→ `results/chat-list-before-summary.md` vs `chat-list-after-summary.md` 비교표를 `compare.md`에 정리.
기대: p95 1497ms → 수십 ms, 쿼리 408ms → 한 자릿수.

---

## 내 도메인에 미러링할 때 체크리스트

- [ ] 1: 내 도메인 유형 분류 + 의심 API/이유 표
- [ ] 2: 의심 구간 Timer (`<도메인>_..._duration`) + `event=<도메인>_...` 로그
- [ ] 3: p95/실패율 + 트래픽 조건
- [ ] 4: `scripts/<도메인>/` 시나리오 (인증이면 `auth.js`, 스프레드/`?.` 금지)
- [ ] 5: `@Profile("loadtest")` 시더로 대량 데이터 → baseline
- [ ] 6: 대시보드 도메인 변수 = 내 도메인, 3증거 교차
- [ ] 7: 최적화 후 `-after` 재측정 → compare.md
