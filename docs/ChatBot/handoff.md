# Handoff: ChatBot 도메인 Clean Architecture 구현

## 현재 상태

**Branch**: `feat/chatbot1`
**작업 단계**: 설계 완료 → **Phase 0 구현 시작 대기**

Plan 승인됨. 코드 아직 한 줄도 안 짬.

## 핵심 산출물

| 산출물 | 경로 |
|--------|------|
| 구현 계획 (확정) | `docs/ChatBot/clean_architecture_plan.md` |
| PRD (FastAPI) | `docs/ChatBot/prd_fastapi.md` |
| Payload 스펙 | `docs/ChatBot/payload_request.md`, `payload_response.md` |
| Handoff (FastAPI) | `docs/ChatBot/handoff_fastapi.md` |
| 컨벤션 | `docs/CONVENTION.md` |
| 참조 프로젝트 | `C:\Lecture\13_Architecture\module13-clean-architecture` |

## 다음 세션이 할 것

**Phase 0: 사전준비 (#180)** — `docs/ChatBot/clean_architecture_plan.md`의 "구현 순서 > Phase 0" 항목 그대로 실행.

순서:
1. `build.gradle` — `spring-boot-starter-webflux` 추가
2. `application-local.yml` — fastapi 설정 추가
3. domain 계층 (순수 Java 모델 + repository 인터페이스 + exception)
4. infrastructure 계층 (JpaEntity + Adapter + FastAPI client DTOs)
5. application 계층 (Port 인터페이스 + ChatContext VO)
6. presentation 계층 (ResponseCode/Message)
7. `./gradlew build` 컴파일 통과

이후 Phase 1~5 순차 진행 (Slice 1~5, 이슈 #181~#185).

## 설계 결정 요약 (17개 — 상세는 plan 참조)

주요 결정만:
- **패키지**: `com.wanted.codebombalms.chatbot.*` (기존 `domain.*`과 분리)
- **Cross-domain**: `ChatContextPort` interface → `ChatContextAdapter`가 기존 layered repo 감쌈
- **데이터 변환**: `ChatContext`(app VO) → `FastApiChatRequest`(infra) 분리
- **Port 반환**: inner record 패턴 (`ChatContextPort.ProblemSetInfo`, `AiChatClient.AiChatClientResponse`)
- **유저 메시지 선커밋**: `ChatMessageSaveService`(REQUIRES_NEW) 별도 빈
- **URL**: `POST /api/v1/chat/{roomId}/messages` (path variable)
- **일반 채팅방**: problemSetId null = 자유 질문 모드 유지

## GitHub 이슈 (ChatBot)


| 이슈 | 내용 | 상태 |
|------|------|------|
| #180 | 사전준비: 도메인 기반 구조 세팅 | Open — **다음 세션 타겟** |
| #181 | Slice 1: 채팅방 생성 | Open |
| #182 | Slice 2: 채팅방 목록 조회 | Open |
| #183 | Slice 3: 채팅 내역 조회 | Open |
| #184 | Slice 4: ChatContextBuilder | Open |
| #185 | Slice 5: FastAPI 호출 + 응답 저장 | Open |
## 주의사항

- `SecurityConfig` `.anyRequest().permitAll()` 상태 — 건드리지 말 것. 별도 이슈.
- userId 추출: `@AuthenticationPrincipal Long userId` (JWT 필터가 principal에 Long 세팅)
- 테스트: `@WebMvcTest`(Controller) + 단위(Service) + `@DataJpaTest`(Adapter) + MockWebServer(FastAPI)
- 기존 도메인은 layered 그대로. chatbot만 클린아키텍쳐.

## 추천 스킬

- `/tdd` — 각 Slice 구현 시 (특히 ChatContextBuilder 분기 테스트)
- `/simplify` — 구현 후 코드 리뷰
