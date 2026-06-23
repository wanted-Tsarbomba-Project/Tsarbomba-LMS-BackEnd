# 챗봇 도메인 — 심은 메트릭/로그 (7단계 §2단계)

> 프로세스 [`../../PROCESS.md`](../../PROCESS.md) · 컨벤션 [`../../CONVENTION.md`](../../CONVENTION.md)
> 트랙 A(N+1 최적화 서사)만 대상. 트랙 B(SSE 한계측정)의 `chat_active_streams` Gauge는 후속.

## 커스텀 메트릭 (Layer 3)

| Prometheus 노출명 | 종류 | 코드 등록명 | 의미 | 코드 위치 |
|---|---|---|---|---|
| `chat_room_list_query_duration_seconds_{count,sum,max}` | Timer | `chat_room_list_query_duration` | 채팅방 목록 조회 구간(제목 fanout 포함) 시간 | [`ChatMetrics`](../../../../src/main/java/com/wanted/codebombalms/chatbot/infrastructure/metrics/ChatMetrics.java), [`ChatRoomQueryService.listRooms`](../../../../src/main/java/com/wanted/codebombalms/chatbot/application/service/ChatRoomQueryService.java#L28) |

PromQL(평균):
```promql
rate(chat_room_list_query_duration_seconds_sum[1m])
/ rate(chat_room_list_query_duration_seconds_count[1m])
```

**왜 이 Timer인가:** `http_server_requests`(요청 전체)는 직렬화·네트워크까지 섞여 "쿼리만의 비용"을 못 가른다. 이 Timer는 목록 쿼리 구간만 분리 → 부하 중 **HTTP p95 ↑ 와 이 timer ↑ 가 동반**하면 병목 = 목록 쿼리(N+1) 확정.

## 구조화 로그 (Loki)

```text
event=chat_room_list_queried resultCount=<n> durationMs=<n>   (+ MDC traceId 자동)
```
- 위치: [`ChatRoomQueryService.listRooms`](../../../../src/main/java/com/wanted/codebombalms/chatbot/application/service/ChatRoomQueryService.java#L28)
- LogQL:
```logql
{job="lms"} |= "event=chat_room_list_queried"
  | regexp "durationMs=(?P<durationMs>[0-9]+)" | unwrap durationMs
```
- 보조 증거: loadtest 프로파일은 `org.hibernate.SQL DEBUG`라 **느린 요청 traceId로 1+2N개 SQL이 로그에 직접 보인다**(N+1 육안 확인).

## 공용 메트릭 (Layer 1·2, 추가 작업 없음)

| 출처 | 무엇 |
|---|---|
| `http_server_requests_seconds{domain="chatbot",uri="/api/v1/chat/list"}` | 요청 전체 p95·RPS·에러율 (DomainObservationConvention) |
| `hikaricp_connections_active` | N+1이 커넥션 점유 늘리는지 |
| `event=request_completed ... durationMs=` (MdcLoggingFilter) | 요청 단위 완료 로그·traceId |
