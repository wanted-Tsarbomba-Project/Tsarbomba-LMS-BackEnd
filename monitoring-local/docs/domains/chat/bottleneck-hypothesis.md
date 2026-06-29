# 챗봇 도메인 — 병목 가설 (Phase 1 파일럿 / 7단계 §1단계)

> 프로세스: [`../../PROCESS.md`](../../PROCESS.md) · 컨벤션: [`../../CONVENTION.md`](../../CONVENTION.md)
> 이 문서는 **부하테스트 전에 "무엇이 왜 느릴 것인가"를 가설로 박는** 산출물이다. baseline·전후비교로 검증한다.

## 도메인 유형 분류

챗봇은 **한 도메인이 두 유형을 동시에** 가진다. 그래서 파일럿 본보기로 적합하다 — 유형별 분기를 한 도메인에서 증명한다.

| 트랙 | 대상 API | 유형 | 6단계 처리 방식 |
|------|----------|------|------------------|
| **A. 최적화 서사** | `GET /api/v1/chat/list` | 조회/집계형 (N+1) | 인덱스가 아니라 **쿼리/페치 리팩토링**으로 해소 → 전후 비교 |
| **B. 한계 측정** | `POST /api/v1/chat/messages`, `POST /api/v1/chat/{roomId}/messages` | 외부연동/스트리밍 (SSE→FastAPI) | 최적화가 아니라 **동시성·타임아웃 한계 측정** |

---

## 병목 가설표

| # | 대상 API | 병목 가설 | 근거 (코드 위치) | 관찰 지표 | 성공 기준 |
|---|----------|-----------|------------------|-----------|-----------|
| 1 ★ | `GET /api/v1/chat/list` | **N+1 + 과조회.** 방 N개 조회 시 `findByUserId` 1번 + 방마다 `ProblemSet상세`·`Problem상세` 2번 = **1+2N 쿼리**. 게다가 title 한 줄 얻으려 detail 집합 전체를 로드. 방이 늘수록 선형 악화 | [`ChatRoomQueryService.toResult`](../../../../src/main/java/com/wanted/codebombalms/chatbot/application/service/ChatRoomQueryService.java#L41) → [`ProblemTitleAdapter`](../../../../src/main/java/com/wanted/codebombalms/chatbot/infrastructure/adapter/ProblemTitleAdapter.java#L21) (`findProblemSetDetail()`/`findProblem()`로 상세 전체 로드) | `http_server_requests_seconds{domain="chatbot",uri="/api/v1/chat/list"}` p95, Hikari active, Loki `durationMs` | `http_req_duration{type:list}` p95 < 500ms, 실패율 < 1% (VU 50, 5분, 방 다수 시드) |
| 2 | `POST /api/v1/chat/messages` (sendFirst) | **외부 의존 지연 + 동시성 한계.** 지연 대부분이 FastAPI 응답. 내 서버 한계 = WebClient 커넥션풀 / MVC async 스레드 / 타임아웃 동작. 진입부 방 준비(tx)는 동기 블로킹 | [`ChatMessageCommandService.send`](../../../../src/main/java/com/wanted/codebombalms/chatbot/application/service/ChatMessageCommandService.java#L22), WebClient 100s / MVC async 120s | async 스레드풀 포화, 활성 SSE 수, 타임아웃 발생률, `event=request_completed`(⚠️ async라 durationMs 부정확) | **합격/불합격 아님.** "외부 느릴 때 몇 동시 스트림까지 5xx/타임아웃 없이 버티나"가 산출물 |
| 3 | `GET /api/v1/chat/{roomId}/messages` | **페이징 없는 전체 내역 로드.** 대화 길어지면 payload·쿼리 비용 증가 | [`ChatMessageQueryService.listMessages`](../../../../src/main/java/com/wanted/codebombalms/chatbot/application/service/ChatMessageQueryService.java#L24) (`findByRoomId` 전체) | 위 list와 동일 패턴, 응답 크기 | 보조 후보 — v1은 가설만, 시간 남으면 측정 |

★ = 파일럿 메인 서사 (전후 비교의 주인공).

---

## 트랙 A 최적화 후보 (6→7단계에서 검증)

N+1이라 인덱스로 안 풀린다. 해소 수단 후보(전후 비교로 택1 증명):

1. **batch 조회** — 방 목록의 `problemSetId`/`problemId`를 모아 `IN` 한 번으로 제목만 조회 (`findTitlesByIds`). 1+2N → 1+2.
2. **제목 전용 경량 조회** — `findProblemSetDetail`(상세 전체) 대신 `select title where id in (...)` 프로젝션. 과조회 제거.
3. (대안) 방 목록에서 제목을 아예 빼고 별도 호출 — API 계약 변경이라 후순위.

> 기대: baseline에서 방 수 ↑일수록 p95 선형 악화 → 최적화 후 평탄화. 전후 비교표(EXPLAIN rows + p95 + Hikari active)로 증명.

## 트랙 B 한계 측정 관점

- FastAPI는 mock 프로파일(`MockAiChatClient`)로 **고정 지연을 주입**해 "외부가 느린 상황"을 재현 (실 FastAPI 부하 회피).
- 동시 SSE를 늘리며: async 스레드 포화 시점, 타임아웃(100s/120s) 동작, 부분답 버림(`Done` 수신 시에만 저장) 정상 여부 관찰.

---

## 다음 단계

- **2단계**: 커스텀 메트릭 심기 — `chat_list_query_*`(또는 트랙B `chat_active_streams` Gauge), `event=chat_*` 로그. → [`metrics.md`](metrics.md)
- **4·5단계**: `monitoring-local/k6/scripts/chat/` 시나리오 + baseline (방 다수 시드 선행, `db/seed/`)
