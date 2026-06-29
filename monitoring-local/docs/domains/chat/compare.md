# 챗봇 트랙 A — baseline 결과 & 추후 변경 지점 (7단계)

> 프로세스 [`../../PROCESS.md`](../../PROCESS.md). baseline 까지 측정 완료, **최적화 적용은 보류(추후 변경 지점)**.

## baseline (N+1 확정) — `chat-list-before`

조건: 방 200개 보유 계정, `GET /api/v1/chat/list`, ramping-vus 0→50, 약 70초.

| 지표 | 값 | 판정 |
|---|---|---|
| k6 `http_req_duration` p95 | **1497ms** | 목표 500ms 3배 초과 → **불합격** |
| k6 `http_req_waiting` p95 | 1493ms | duration과 동일 → 서버/DB 바운드 |
| 커스텀 `chat_room_list_query_duration` avg | **408ms** | 목록 쿼리 구간만 408ms |
| `http_req_failed` | 0% | 기능 정상(느린 것뿐) |

**결론**: `GET /api/v1/chat/list` 1회 = `1 + 2N`쿼리(N=방 수). [`ChatRoomQueryService.toResult`](../../../../src/main/java/com/wanted/codebombalms/chatbot/application/service/ChatRoomQueryService.java#L41)가 방마다 [`ProblemTitleAdapter`](../../../../src/main/java/com/wanted/codebombalms/chatbot/infrastructure/adapter/ProblemTitleAdapter.java)로 제목 2회 조회 → 전형적 N+1. raw 결과: `monitoring-local/k6/results/chat-list-before-summary.md`.

## ⏸ 추후 변경 지점 (최적화 보류)

batch 조회로 `1+2N → 1+2` 가 정석이나 **`problems` BC(타 담당자 도메인)의 repo/port/service 3층을 건드려야** 해서 이번 파일럿 범위에서 제외. 적용 시 변경 지점:

| 레이어 | 파일 | 추가할 것 |
|---|---|---|
| chat port | `ProblemTitlePort` | `Map<Long,String> findProblemSetTitlesByIds(Collection)` / `findProblemTitlesByIds(Collection)` |
| chat adapter | `ProblemTitleAdapter` | 위 batch 위임 |
| chat service | `ChatRoomQueryService.listRooms` | 방들에서 id 모아 batch 2회 호출 → Map 매핑 |
| problems set | `ProblemSetQueryService` (+ `LoadProblemSetPort`, adapter, SpringData) | id IN 제목 조회 (additive) |
| problems | `ProblemQueryService` (+ `ProblemRepository`, adapter, SpringData) | id IN 제목 조회 (additive) |

> 기대: p95 1497ms → 수십 ms, 쿼리 408ms → 한 자릿수. 적용 후 `chat-list-after` 로 재측정해 이 표를 전후 비교로 채운다.
> 대안(차선): 챗봇 안에서 problemSetId dedup만 → `1+2N → 1+distinctSet+N`. N+1 절반만 해소.
