# 챗봇 도메인 — 심은 메트릭/로그 (7단계 §2단계)

> 프로세스 [`../../PROCESS.md`](../../PROCESS.md) · 컨벤션 [`../../CONVENTION.md`](../../CONVENTION.md)
> 트랙 A(N+1 최적화)·트랙 B(SSE 운영) 모두 대상. 운영 대시보드: [`lms-chat-ops-dashboard.json`](../../../grafana/provisioning/dashboards/lms-chat-ops-dashboard.json).

## 커스텀 메트릭 (Layer 3)

| Prometheus 노출명 | 종류 | 코드 등록명 | 의미 | 코드 위치 |
|---|---|---|---|---|
| `chat_room_list_query_duration_seconds_{count,sum,max}` | Timer | `chat_room_list_query_duration` | 채팅방 목록 조회 구간(제목 fanout 포함) 시간 | [`ChatMetrics`](../../../../src/main/java/com/wanted/codebombalms/chatbot/infrastructure/metrics/ChatMetrics.java), [`ChatRoomQueryService.listRooms`](../../../../src/main/java/com/wanted/codebombalms/chatbot/application/service/ChatRoomQueryService.java#L28) |
| `chat_prompt_tokens_total` / `chat_completion_tokens_total` | Counter | `chat_prompt_tokens` / `chat_completion_tokens` | AI 입력/출력 토큰 누적(=비용). 단가 달라 분리 | [`ChatMetrics`](../../../../src/main/java/com/wanted/codebombalms/chatbot/infrastructure/metrics/ChatMetrics.java) |
| `chat_active_streams` | Gauge | `chat_active_streams` | 현재 동시 SSE 스트림 수 | [`ChatMetrics`](../../../../src/main/java/com/wanted/codebombalms/chatbot/infrastructure/metrics/ChatMetrics.java), [`ChatMessageCommandService.send`](../../../../src/main/java/com/wanted/codebombalms/chatbot/application/service/ChatMessageCommandService.java) |
| `chat_ai_stream_duration_seconds_*` (tag `outcome`) | Timer(histogram) | `chat_ai_stream_duration` | AI 스트림 전체 소요(+실패율: `outcome=error`) | 〃 |
| `chat_ai_time_to_first_token_seconds_*` | Timer(histogram) | `chat_ai_time_to_first_token` | 구독~첫 토큰(TTFT, 체감 지연) | 〃 |
| `chat_stream_terminations_total` (tag `signal`) | Counter | `chat_stream_terminations` | 종료 신호 분포(`onComplete` vs `cancel`/`onError`=중도이탈) | 〃 |

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

### 스트림 에러/추적 로그 ([`ChatMessageCommandService.send`](../../../../src/main/java/com/wanted/codebombalms/chatbot/application/service/ChatMessageCommandService.java))

SSE 콜백은 별도 reactor 스레드라 MDC가 전파 안 된다 → 식별자(`userId`/`roomId`/`traceId`)를 `command`에서 꺼내 **명시적으로** 박는다. 유저 입력 원문은 로깅 안 함(`userMessageLength`만).

```text
event=chat_ai_error_chunk  userId=.. roomId=.. code=.. traceId=..      # FastAPI가 error 프레임
event=chat_save_failed     userId=.. roomId=.. traceId=..              # 답변 저장 실패
event=chat_stream_aborted  userId=.. roomId=.. exceptionType=.. traceId=..
event=chat_stream_end      userId=.. roomId=.. signal=.. outcome=.. durationMs=.. userMessageLength=.. traceId=..  # 정상 포함 추적 기준선
```

### traceId 전파 (BE ↔ FastAPI)

`MdcLoggingFilter` 생성 traceId → 컨트롤러에서 캡처해 command → 스트림 로그 + `FastApiChatClient`가 **`X-Trace-Id` 헤더로 FastAPI에 전파**. FastAPI(`tsarbombaChatBot`)는 헤더를 받아 `trace_id=`로 로깅 → 두 서비스 로그가 같은 값으로 엮인다.

```logql
{job=~".+"} |= "a1b2c3d4"   # 값으로 매칭 → BE(traceId=)·FastAPI(trace_id=) 양쪽 다 잡힘
```

## 공용 메트릭 (Layer 1·2, 추가 작업 없음)

| 출처 | 무엇 |
|---|---|
| `http_server_requests_seconds{domain="chatbot",uri="/api/v1/chat/list"}` | 요청 전체 p95·RPS·에러율 (DomainObservationConvention) |
| `hikaricp_connections_active` | N+1이 커넥션 점유 늘리는지 |
| `event=request_completed ... durationMs=` (MdcLoggingFilter) | 요청 단위 완료 로그·traceId |
