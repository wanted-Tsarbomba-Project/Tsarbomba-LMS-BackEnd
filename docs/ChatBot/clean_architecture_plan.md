# ChatBot 도메인 — Clean Architecture 구조 설계 및 구현 계획

## Context

현재 LMS 프로젝트는 layered 구조(`domain/{feature}/controller|service|entity|repository|dto`).
ChatBot 도메인은 `module13-clean-architecture` 참조 프로젝트 패턴으로 클린아키텍쳐 전환.
Spring Phase 1 = 이슈 #180~#185 (사전준비 → Slice 5).
기존 layered 도메인(ProblemSet, Problem 등)은 Port 인터페이스로 감싸서 접근.

## 컨벤션 (docs/CONVENTION.md 기준)

- 에러코드: `CHT-001` 형식 (chatbot 접두어 `CHT`)
- 예외: 개별 Exception 클래스 금지 → `NotFoundException(ChatErrorCode.XXX)` 사용
- 성공 응답: `ApiResponse.success()` / `ApiResponse.created()`
- ResponseCode/Message: presentation 패키지에 `ChatResponseCode.java` + `ChatResponseMessage.java`
- Swagger: `@Operation` + `@ApiResponses` (도메인 에러만 명시)

## 설계 결정 (Grill-me 확정)

| # | 결정 | 선택 |
|---|------|------|
| Q1 | ErrorCode 위치 | `chatbot/domain/exception/ChatErrorCode.java` (기존 패턴) |
| Q2 | 도메인 모델 예외 | `ForbiddenException` 기존 패턴 그대로 |
| Q3 | application↔infrastructure 경계 | `ChatContext`(application VO) 분리 → infrastructure에서 `FastApiChatRequest`로 변환 |
| Q4 | 유저 메시지 선커밋 | `ChatMessageSaveService`(REQUIRES_NEW) 분리 |
| Q5 | UseCase 인터페이스 | 1 UseCase = 1 인터페이스 (module13 패턴) |
| Q6 | problemSetTitle 조회 | Repository JOIN 쿼리 + `ChatRoomWithTitle` VO |
| Q7 | 메시지 전송 URL | `POST /api/v1/chat/{roomId}/messages` (path variable) |
| Q8 | ChatContextPort VO | inner record로 응집 |
| Q9 | AiChatClient 반환 | `AiChatClientResponse` inner record (port 전용) |
| Q10 | ChatContextBuilder 위치 | `application/service/` 유지 |
| Q11 | SecurityConfig | 안 건드림. 별도 이슈 |
| Q12 | userId 추출 | `@AuthenticationPrincipal Long userId` |
| Q13 | conversationHistory | `ChatMessageRepository` 직접 조회 (cross-domain 아님) |
| Q14 | 일반 채팅방 | 유지. problemSetId null = 자유 질문 모드 |
| Q15 | ChatContext 구조 | 단일 record + nullable 필드 |
| Q16 | FastApiChatRequest | 1개 + `@JsonInclude(NON_NULL)` |
| Q17 | ChatContextBuilder.build() 시그니처 | `build(SendMessageCommand command, ChatRoom chatRoom)` |

## ChatBot 패키지 구조

```
com.wanted.codebombalms.chatbot/
├── presentation/
│   └── api/
│       ├── ChatRoomController.java              ← POST /api/v1/chat, GET /api/v1/chat/list
│       ├── ChatMessageController.java           ← GET  /api/v1/chat/{roomId}/messages
│       │                                          POST /api/v1/chat/{roomId}/messages
│       ├── ChatResponseCode.java                ← 성공 코드 상수
│       ├── ChatResponseMessage.java             ← 성공 메시지 상수
│       ├── request/
│       │   ├── ChatRoomCreateRequest.java       ← record { Long problemSetId }
│       │   └── ChatMessageRequest.java          ← record { Long problemId, String userMessage }
│       └── response/
│           ├── ChatRoomResponse.java            ← record { Long roomId, String problemSetTitle, String lastMessage, Instant updatedAt }
│           ├── ChatMessageResponse.java         ← record { Long messageId, String role, String content, Instant createdAt }
│           └── AiChatResponse.java              ← record { String answer }
│
├── application/
│   ├── usecase/
│   │   ├── CreateChatRoomUseCase.java           ← Long handle(CreateChatRoomCommand)
│   │   ├── ListChatRoomsUseCase.java            ← List<ChatRoomResult> handle(Long userId)
│   │   ├── GetChatMessagesUseCase.java          ← List<ChatMessageResult> handle(Long roomId, Long userId)
│   │   └── SendMessageUseCase.java              ← AiChatResult handle(SendMessageCommand)
│   ├── command/
│   │   ├── CreateChatRoomCommand.java           ← record(Long userId, Long problemSetId)
│   │   └── SendMessageCommand.java              ← record(Long userId, Long roomId, Long problemId, String userMessage)
│   ├── result/
│   │   ├── ChatRoomResult.java                  ← application → presentation 전달용
│   │   ├── ChatMessageResult.java
│   │   └── AiChatResult.java                    ← answer + isAnswerDetected + retryCount + tokenUsage
│   ├── service/
│   │   ├── ChatRoomCommandService.java          ← implements CreateChatRoomUseCase
│   │   ├── ChatRoomQueryService.java            ← implements ListChatRoomsUseCase, GetChatMessagesUseCase
│   │   ├── ChatMessageCommandService.java       ← implements SendMessageUseCase (오케스트레이션)
│   │   ├── ChatMessageSaveService.java          ← 유저 메시지 저장 전용 (REQUIRES_NEW)
│   │   └── ChatContextBuilder.java              ← build(SendMessageCommand, ChatRoom) → ChatContext
│   ├── model/
│   │   └── ChatContext.java                     ← application VO (nullable 필드, 단일 record)
│   └── port/
│       ├── AiChatClient.java                    ← interface + inner record AiChatClientResponse, TokenUsage
│       └── ChatContextPort.java                 ← interface + inner record ProblemSetInfo, ProblemInfo,
│                                                   SubmissionInfo, SessionProgressInfo, DatasetInfo
│
├── domain/
│   ├── model/
│   │   ├── ChatRoom.java                        ← 순수 도메인 모델 (create/restore 팩토리)
│   │   ├── ChatMessage.java                     ← 순수 도메인 모델
│   │   └── MessageRole.java                     ← enum (USER, AI)
│   ├── repository/
│   │   ├── ChatRoomRepository.java              ← interface (outbound port)
│   │   │                                          findByUserIdAndProblemSetId, findByUserIdWithTitle 등
│   │   └── ChatMessageRepository.java           ← interface (outbound port)
│   │                                              findRecentByRoomId(roomId, limit) 포함
│   └── exception/
│       └── ChatErrorCode.java                   ← enum implements ErrorCode
│
└── infrastructure/
    ├── persistence/
    │   ├── ChatRoomJpaEntity.java
    │   ├── ChatMessageJpaEntity.java
    │   ├── SpringDataChatRoomRepository.java        ← extends JpaRepository
    │   ├── SpringDataChatMessageRepository.java
    │   ├── ChatRoomRepositoryAdapter.java           ← implements ChatRoomRepository (JOIN 쿼리 포함)
    │   └── ChatMessageRepositoryAdapter.java        ← implements ChatMessageRepository
    ├── client/
    │   ├── FastApiChatClient.java                   ← implements AiChatClient
    │   │                                              ChatContext → FastApiChatRequest 변환 + WebClient 호출
    │   │                                              FastApiChatResponse → AiChatClientResponse 변환
    │   ├── FastApiChatRequest.java                  ← @JsonInclude(NON_NULL), payload_request.md 기준
    │   ├── FastApiChatResponse.java                 ← payload_response.md 기준
    │   └── FastApiProperties.java                   ← @ConfigurationProperties
    └── adapter/
        └── ChatContextAdapter.java                  ← implements ChatContextPort
                                                        기존 layered repo 주입 → inner record VO 변환
```

## 의존성 방향 (Clean Architecture 규칙)

```
presentation → application → domain ← infrastructure
                    ↑                       ↑
              (UseCase Port)        (Repository Port, AiChatClient Port, ChatContextPort)
```

- **domain**: 프레임워크 의존 X. 순수 Java + global 공통 예외만 허용.
- **application**: domain만 의존. Spring `@Service`/`@Transactional` 허용. Port 인터페이스로 외부 접근.
- **infrastructure**: domain + application port 구현. JPA, WebClient, 기존 layered repo 의존.
- **presentation**: application usecase만 의존. HTTP 관심사만.

### Cross-Domain 접근 (Port 감싸기)

```
ChatContextBuilder (application)
  → ChatContextPort (application/port — interface)
    → ChatContextAdapter (infrastructure/adapter — 구현체)
      → ProblemSetRepository, ProblemRepository, SubmissionRepository,
        ProgressRepository, ProblemDatasetRepository (기존 layered)
```

### 데이터 변환 흐름 (SendMessage)

```
SendMessageCommand + ChatRoom
  → ChatContextBuilder.build() → ChatContext (application VO)
    → AiChatClient.call(ChatContext)
      → FastApiChatClient: ChatContext → FastApiChatRequest 변환 → HTTP POST
      → FastApiChatClient: FastApiChatResponse → AiChatClientResponse 변환
        → ChatMessageCommandService: AiChatClientResponse → AiChatResult 변환
          → Controller: AiChatResult → AiChatResponse 변환
```

## API 엔드포인트

| Method | URL | Controller | UseCase |
|--------|-----|------------|---------|
| POST | `/api/v1/chat` | ChatRoomController | CreateChatRoomUseCase |
| GET | `/api/v1/chat/list` | ChatRoomController | ListChatRoomsUseCase |
| GET | `/api/v1/chat/{roomId}/messages` | ChatMessageController | GetChatMessagesUseCase |
| POST | `/api/v1/chat/{roomId}/messages` | ChatMessageController | SendMessageUseCase |

## 핵심 도메인 모델 설계

### ChatRoom (순수 Java)
```java
public class ChatRoom {
    private final Long id;
    private final Long userId;
    private final Long problemSetId;  // nullable (일반 채팅방 = 자유 질문 모드)
    private Instant createdAt;
    private Instant updatedAt;

    public static ChatRoom create(Long userId, Long problemSetId) { ... }
    public static ChatRoom restore(Long id, Long userId, Long problemSetId, Instant createdAt, Instant updatedAt) { ... }
    public void updateTimestamp(Instant now) { ... }
    public void verifyOwner(Long requestUserId) { ... }  // → ForbiddenException(ChatErrorCode.CHAT_ROOM_FORBIDDEN)
}
```

### ChatMessage (순수 Java)
```java
public class ChatMessage {
    private final Long id;
    private final Long roomId;
    private final MessageRole role;
    private final String content;
    private final Instant createdAt;

    public static ChatMessage createUserMessage(Long roomId, String content) { ... }
    public static ChatMessage createAiMessage(Long roomId, String content) { ... }
    public static ChatMessage restore(Long id, Long roomId, MessageRole role, String content, Instant createdAt) { ... }
}
```

### Port 인터페이스

```java
public interface ChatContextPort {
    ProblemSetInfo findProblemSet(Long problemSetId);
    ProblemInfo findProblem(Long problemId);
    SubmissionInfo findLatestSubmission(Long userId, Long problemId);
    SessionProgressInfo findSessionProgress(Long problemSetId);
    DatasetInfo findDataset(Long problemId);

    record ProblemSetInfo(String title, String description, String difficulty, String categoryName) {}
    record ProblemInfo(Long problemId, int problemOrder, String title, String content,
                       String problemType, String answer, String explanation) {}
    record SubmissionInfo(String submittedCode, String submittedAnswer, boolean isCorrect,
                          String executionStatus, String errorMessage,
                          Integer passedTestCount, Integer totalTestCount) {}
    record SessionProgressInfo(int currentProblemNumber, int totalProblemCount, List<String> problemTitles) {}
    record DatasetInfo(String fileName, String fileUrl) {}
}
```

```java
public interface AiChatClient {
    AiChatClientResponse call(ChatContext context);

    record AiChatClientResponse(String answer, boolean isAnswerDetected, int retryCount, TokenUsage tokenUsage) {}
    record TokenUsage(int promptTokens, int completionTokens, int totalTokens) {}
}
```

## 구현 순서 (이슈 매핑)

### Phase 0: 사전준비 (#180)
1. `build.gradle` — `spring-boot-starter-webflux` 추가
2. `application-local.yml` — `fastapi.url`, `chat.max-history-messages` 설정 추가
3. domain/model — `ChatRoom`, `ChatMessage`, `MessageRole` (순수 Java)
4. domain/repository — `ChatRoomRepository`, `ChatMessageRepository` 인터페이스
5. domain/exception — `ChatErrorCode` enum
6. infrastructure/persistence — JpaEntity + SpringDataRepo + Adapter (JOIN 쿼리 포함)
7. infrastructure/client — `FastApiProperties`, `FastApiChatRequest`, `FastApiChatResponse`
8. application/port — `AiChatClient`(+inner records), `ChatContextPort`(+inner records) 인터페이스
9. application/model — `ChatContext` record
10. infrastructure/adapter — `ChatContextAdapter` 구현
11. presentation — `ChatResponseCode`, `ChatResponseMessage`
12. `./gradlew build` 컴파일 통과

### Phase 1: Slice 1 — 채팅방 생성 (#181)
1. `CreateChatRoomCommand` record
2. `CreateChatRoomUseCase` interface
3. `ChatRoomCommandService` implements UseCase
   - 같은 userId+problemSetId 존재 → 기존 방 반환 (200)
   - 없으면 → `ChatRoom.create()` → save (201)
4. `ChatRoomCreateRequest`, `ChatRoomResponse`, `ChatRoomResult`
5. `ChatRoomController` — `POST /api/v1/chat` (`@AuthenticationPrincipal Long userId`)
6. 테스트: Controller(`@WebMvcTest`), Service(단위), Domain(순수)

### Phase 2: Slice 2 — 채팅방 목록 조회 (#182)
1. `ListChatRoomsUseCase` interface
2. `ChatRoomQueryService` — userId 기준, JOIN으로 problemSetTitle 포함, updated_at DESC
3. `ChatRoomController` — `GET /api/v1/chat/list`
4. 테스트

### Phase 3: Slice 3 — 채팅 내역 조회 (#183)
1. `GetChatMessagesUseCase` interface
2. `ChatRoomQueryService` — 소유권 검증(`ChatRoom.verifyOwner`) + 메시지 조회
3. `ChatMessageController` — `GET /api/v1/chat/{roomId}/messages`
4. 테스트

### Phase 4: Slice 4 — ChatContextBuilder (#184)
1. `ChatContextPort` inner VO 정의 완료
2. `ChatContextAdapter` 구현 — 기존 layered repo 조회 → inner record 변환
3. `ChatContextBuilder.build(SendMessageCommand, ChatRoom)` → `ChatContext`
   - TEXT/CODE 분기
   - submission 유무 분기
   - problemId null 분기 (일반 질문 모드)
   - conversationHistory — `ChatMessageRepository`에서 최근 20개
4. 테스트 (각 분기 케이스, Port mock)

### Phase 5: Slice 5 — FastAPI 호출 + 응답 저장 (#185)
1. `SendMessageCommand` record
2. `SendMessageUseCase` interface
3. `FastApiChatClient` implements AiChatClient
   - `ChatContext` → `FastApiChatRequest` 변환
   - WebClient POST 호출
   - `FastApiChatResponse` → `AiChatClientResponse` 변환
4. `ChatMessageSaveService` — 유저 메시지 저장 (REQUIRES_NEW)
5. `ChatMessageCommandService` implements SendMessageUseCase (오케스트레이션)
   - ChatRoom 조회 + `verifyOwner()`
   - `ChatMessageSaveService.save()` — 유저 메시지 선커밋
   - `ChatContextBuilder.build()` → `ChatContext`
   - `AiChatClient.call(ChatContext)` → `AiChatClientResponse`
   - AI 응답 DB 저장
   - `chatRoom.updateTimestamp()` → save
   - `AiChatClientResponse` → `AiChatResult` 변환 → 반환
6. `ChatMessageController` — `POST /api/v1/chat/{roomId}/messages`
7. 테스트: MockWebServer(FastAPI), Service(단위), Controller(`@WebMvcTest`)

## 기존 코드 재사용

| 항목 | 위치 |
|------|------|
| `ApiResponse<T>` | `global/presentation/api/common/ApiResponse.java` |
| `ErrorCode` interface | `global/domain/common/error/ErrorCode.java` |
| `DomainException` | `global/domain/common/error/DomainException.java` |
| `NotFoundException`, `ForbiddenException` | `global/domain/common/error/exception/` |
| `JwtAuthenticationFilter` | `global/infrastructure/jwt/` (userId를 principal에 세팅) |
| 기존 repo들 | `ChatContextAdapter`에서만 주입 (port 뒤에 숨김) |

## Verification

1. `./gradlew build` — 컴파일 통과
2. 각 Slice 별 테스트 통과 (H2 in-memory)
3. Swagger UI — 4개 엔드포인트 노출 확인
4. 의존성 방향 확인: domain 패키지에 JPA/Spring import 없음
5. ChatContextPort를 통한 cross-domain 접근 — 기존 엔티티 chatbot에 직접 노출 없음
6. 데이터 변환 체인: ChatContext → FastApiChatRequest → FastApiChatResponse → AiChatClientResponse → AiChatResult → AiChatResponse
