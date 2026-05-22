# ChatBot 도메인 개발 가이드 (CLAUDE.md)

> 다음 세션/팀원이 이 문서만 보고도 동일한 컨벤션으로 ChatBot 도메인 코드를 추가/수정할 수 있게 작성한 가이드.
> 글로벌 컨벤션은 `docs/CONVENTION.md` 참조. 본 문서는 ChatBot 전용 추가 규칙.

---

## 1. 개요

- **패키지**: `com.wanted.codebombalms.chatbot.*`
- **아키텍쳐**: 클린아키텍쳐 4계층 (다른 도메인은 layered 구조이지만 ChatBot만 분리)
- **의존성 방향**:
  ```
  presentation → application → domain ← infrastructure
                     ↑                        ↑
              (UseCase Port)         (Repository Port, AiChatClient, ChatContextPort)
  ```
- **규칙 요약**:
  - `domain`: 순수 Java, 프레임워크 import 금지
  - `application`: domain만 의존, Spring `@Service`/`@Transactional` 허용, 외부는 Port interface로 접근
  - `infrastructure`: domain + application port 구현체. JPA/WebClient/타 도메인 layered repo는 여기서만 의존
  - `presentation`: application의 UseCase만 의존. HTTP 관심사 한정

---

## 2. 패키지 구조 (한눈에)

```
chatbot/
├── presentation/api/
│   ├── {Domain}Controller.java                  ← ChatRoomController, ChatMessageController
│   ├── ChatResponseCode.java
│   ├── ChatResponseMessage.java
│   ├── request/                                 ← XxxRequest record (HTTP body)
│   └── response/                                ← XxxResponse record (+ static from(Result))
├── application/
│   ├── usecase/                                 ← {Domain}CommandUseCase / {Domain}QueryUseCase
│   ├── service/                                 ← {Domain}CommandService / {Domain}QueryService
│   ├── command/                                 ← XxxCommand record (Controller → Service 입력)
│   ├── result/                                  ← XxxResult record (Service → Controller 출력)
│   ├── model/                                   ← ChatContext 같은 application VO
│   └── port/                                    ← outbound port interface + inner record
├── domain/
│   ├── model/                                   ← ChatRoom, ChatMessage, MessageRole (순수 Java)
│   ├── repository/                              ← Repository interface (+ default getById)
│   └── exception/                               ← ChatErrorCode enum
└── infrastructure/
    ├── persistence/                             ← JpaEntity, Mapper, SpringDataRepo, RepositoryAdapter
    ├── client/                                  ← FastApi DTO, WebClient impl, WebClientConfig, Properties
    └── adapter/                                 ← Cross-domain Port impl (ChatContextAdapter)
```

---

## 3. 핵심 컨벤션

### 3.1 UseCase = Command/Query 2개로 분리

**규칙**: 1 메서드 = 1 interface 금지. 도메인 대상별 Command/Query 2개로 묶음.

```java
// ChatRoom 도메인
public interface ChatRoomCommandUseCase {
    ChatRoomResult create(CreateChatRoomCommand command);
    void delete(Long roomId, Long userId);
}

public interface ChatRoomQueryUseCase {
    List<ChatRoomResult> listRooms(Long userId);
}
```

새 행위 추가 시 새 UseCase 만들지 말고 기존 Command/Query에 메서드 추가.
**대상 도메인이 다를 때만** 새 UseCase 생성 (예: `ChatMessageCommandUseCase`).

### 3.2 Service 명명 및 트랜잭션

| 종류 | 이름 | 어노테이션 |
|------|------|-----------|
| Command | `{Domain}CommandService` | `@Service @RequiredArgsConstructor @Transactional` |
| Query | `{Domain}QueryService` | `@Service @RequiredArgsConstructor @Transactional(readOnly = true)` |

- `REQUIRES_NEW`가 필요하면 **클래스가 아닌 메서드 단위**에 달기 → 의도 명확화
- 클래스 `@Transactional` + 메서드 `@Transactional(propagation = REQUIRES_NEW)` 조합 가능

### 3.3 도메인 모델 (`domain/model`)

- **순수 Java**. JPA/Spring annotation 금지.
- 두 팩토리 메서드:
  - `create()` — 새 객체 (id null)
  - `restore()` — DB에서 복원 (id 보유)
- 비즈니스 규칙은 도메인 모델 메서드로:
  ```java
  chatRoom.verifyOwner(userId);   // 권한 검증 → ForbiddenException
  chatRoom.updateTimestamp(now);  // 상태 변경
  ```

> `ChatContext`는 application VO이지 도메인 모델 아님. 데이터 운반체는 `application/model`로.

### 3.4 Repository Port + `getById` 정책

- domain/repository에 interface 선언, infrastructure에 Adapter 구현
- 모든 Repository는 `getById` default 메서드 제공:
  ```java
  default ChatRoom getById(Long id) {
      return findById(id)
              .orElseThrow(() -> new NotFoundException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
  }
  ```
- Service에서 `orElseThrow` 반복 금지. `getById()`로 일관된 에러 정책 적용.

### 3.5 Mapper는 별도 클래스

- `infrastructure/persistence/XxxMapper.java`
- `private` 생성자 + `public static toDomain()`/`toEntity()`
- **Adapter는 "행위", Mapper는 "변환"** — 책임 분리.

```java
public class ChatRoomMapper {
    private ChatRoomMapper() {}
    public static ChatRoom toDomain(ChatRoomJpaEntity entity) { ... }
    public static ChatRoomJpaEntity toEntity(ChatRoom domain) { ... }
}
```

### 3.6 Application Port + inner record

외부 시스템 호출(AI, 타 도메인 데이터)은 `application/port`의 interface로 추상화.
응답 타입은 **interface 안 inner record**로 응집:

```java
public interface AiChatClient {
    AiChatClientResponse call(ChatContext context);

    record AiChatClientResponse(String answer, boolean isAnswerDetected,
                                int retryCount, TokenUsage tokenUsage) {}
    record TokenUsage(int promptTokens, int completionTokens, int totalTokens) {}
}
```

`ChatContextPort.ProblemSetInfo`, `ChatContextPort.ProblemInfo` 식으로 함께 응집.

### 3.7 Cross-Domain 접근 = Port로 감싸기

**금지**: chatbot에서 `problems.*`, `submission.*` 등 layered repo 직접 import.

**필수**:
```
chatbot/application/port/ChatContextPort           (interface)
chatbot/infrastructure/adapter/ChatContextAdapter  (구현 — 기존 layered repo 주입)
```

Adapter는 **chatbot 패키지 안**에 있어야 의존 방향(`chatbot → 기존 도메인`) 보존.
반대로 만들면(`problems → chatbot`) 클린아키텍쳐 위반.

### 3.8 데이터 변환 체인

경계마다 변환. presentation이 domain 모델을 직접 노출하면 안 됨.

```
HTTP Request → XxxRequest (record)
            → XxxCommand  (application/command)
            → [Service: Domain 객체 + Port 호출]
            → XxxResult   (application/result)
            → XxxResponse (presentation, static from(Result))
            → HTTP Response
```

메시지 전송의 외부 호출 흐름:
```
ChatContext → FastApiChatRequest → HTTP → FastApiChatResponse → AiChatClientResponse → AiChatResult → AiChatResponse
```

### 3.9 에러코드

- 접두어 **`CHT-XXX`**
- `ChatErrorCode enum implements ErrorCode`
- **개별 Exception 클래스 만들지 말 것** → 공통 예외 + 에러코드:
  ```java
  throw new NotFoundException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
  throw new ForbiddenException(ChatErrorCode.CHAT_ROOM_FORBIDDEN);
  ```

현재 코드:
| 코드 | 의미 |
|------|------|
| `CHT-001` | 채팅방을 찾을 수 없음 |
| `CHT-002` | 채팅방 접근 권한 없음 |
| `CHT-003` | AI 응답 생성 실패 |

### 3.10 인터페이스 주석 스타일

interface의 모든 추상 메서드 위에 **한 줄 주석**, 메서드 사이 **빈 줄 1개**:

```java
public interface ChatRoomRepository {

    // 채팅방 저장 (생성/수정)
    ChatRoom save(ChatRoom chatRoom);

    // id로 채팅방 단건 조회
    Optional<ChatRoom> findById(Long id);

    // userId로 채팅방 목록 조회
    List<ChatRoom> findByUserId(Long userId);
}
```

### 3.11 Swagger 작성 — `@Operation`만 사용

**`@ApiResponses` / `@ApiResponse` 사용 금지.**
이유: Swagger의 `io.swagger.v3.oas.annotations.responses.ApiResponse`가
글로벌 `com.wanted.codebombalms.global.presentation.api.common.ApiResponse`와 이름 충돌.
Java는 import alias가 없어 한쪽을 전체 경로로 써야 하는데 가독성 떨어짐.

**대안**: `@Operation`의 `description`에 상태 코드 + 에러코드 명시.

```java
@Operation(
    summary = "메시지 전송",
    description = "유저 메시지 저장 + FastAPI 호출 + AI 응답 반환 | "
                + "에러: CHT-001 채팅방 없음, CHT-002 권한없음, CHT-003 AI 응답 실패"
)
@PostMapping("/{roomId}/messages")
```

### 3.12 ResponseCode / ResponseMessage

위치: `chatbot/presentation/api/ChatResponseCode.java` + `ChatResponseMessage.java`
형식: `private` 생성자 + `public static final String` 상수
코드 네이밍: `CHAT-{ACTION}` (예: `CHAT-ROOM-CREATED`, `CHAT-MESSAGE-SENT`)

```java
public class ChatResponseCode {
    private ChatResponseCode() {}
    public static final String ROOM_CREATED   = "CHAT-ROOM-CREATED";
    public static final String ROOM_RETRIEVED = "CHAT-ROOM-RETRIEVED";
    public static final String ROOM_DELETED   = "CHAT-ROOM-DELETED";
    public static final String MESSAGES_RETRIEVED = "CHAT-MESSAGES-RETRIEVED";
    public static final String MESSAGE_SENT   = "CHAT-MESSAGE-SENT";
}
```

---

## 4. 새 기능 추가 체크리스트

1. **비즈니스 규칙인가?** → `domain/model`에 메서드로. 데이터 운반체면 `application/model`.
2. **UseCase**: 기존 `{Domain}CommandUseCase` / `{Domain}QueryUseCase`에 메서드 추가. 새 인터페이스 만들지 말 것.
3. **Service**: 같은 도메인의 Command/Query Service에 메서드 추가.
4. **Repository 변경 시**: domain interface ↔ infrastructure Adapter 둘 다 수정.
5. **Cross-domain 데이터 필요**: `ChatContextPort`에 메서드 + inner record 추가 → `ChatContextAdapter`에 구현. 직접 import 금지.
6. **외부 시스템 호출**: `application/port`에 interface, `infrastructure/client` 또는 `infrastructure/adapter`에 구현.
7. **Controller 엔드포인트**:
   - URL은 `/api/v1/chat/...` 하위
   - `@Operation(summary, description)` — description에 상태코드 + 에러코드
   - Request/Response record + Command/Result record로 경계 변환
8. **ResponseCode/Message** 상수 추가.
9. **에러**: 새 에러는 `ChatErrorCode` enum에 `CHT-XXX` 형식으로.
10. **`./gradlew build` 통과** 확인.

---

## 5. 흔한 함정 (실제 막혔던 포인트)

### 5.1 Swagger `@ApiResponse` import 충돌
- `io.swagger.v3.oas.annotations.responses.ApiResponse` ↔ 글로벌 `ApiResponse` 이름 충돌
- **해결**: `@ApiResponses` 자체를 안 쓴다. `@Operation`의 `description`에 에러 명시.

### 5.2 테스트 설정
- `./gradlew build` 시 Spring은 **`src/test/resources/application.yml`** 을 읽음 (`application-local.yml` 안 읽음)
- 새 설정 키 추가 시 테스트 yml에도 같이 추가:
  ```yaml
  fastapi:
    url: http://localhost:8000
  chat:
    max-history-messages: 20
  ```

### 5.3 Lombok `@Getter` + `Boolean isXxx` 필드
- 필드 `Boolean isCorrect` → getter는 `isCorrect()`가 아니라 **`getCorrect()`**
- Lombok이 `is` 접두사 제거 후 `get` 붙임

### 5.4 WebClient 빈
- `WebClient` 기본 빈 없음
- `WebClientConfig`에서 `FastApiProperties.url`을 `baseUrl()`로 세팅한 빈 생성 필수

### 5.5 `JpaRepository.findFirstByXxx` 메서드 시그니처
- JPA 메서드 네이밍 규칙 그대로 따르기 (`findFirstByProblem_ProblemIdAndStatus` 등)
- 실제 엔티티 필드/관계명과 정확히 일치해야 함

### 5.6 SpringDataRepo는 자동 구현, Adapter는 Mapper 호출만
- `extends JpaRepository`에 메서드 네이밍만 맞으면 Spring Data가 구현 자동 생성
- Adapter는 SpringDataRepo + Mapper 조합으로 도메인 객체 반환

---

## 6. API 엔드포인트 (현재 구현)

| Method | URL | UseCase 메서드 |
|--------|-----|---------------|
| POST   | `/api/v1/chat`                       | `ChatRoomCommandUseCase.create`        |
| GET    | `/api/v1/chat/list`                  | `ChatRoomQueryUseCase.listRooms`       |
| DELETE | `/api/v1/chat/{roomId}`              | `ChatRoomCommandUseCase.delete`        |
| GET    | `/api/v1/chat/{roomId}/messages`     | `ChatMessageQueryUseCase.listMessages` |
| POST   | `/api/v1/chat/{roomId}/messages`     | `ChatMessageCommandUseCase.send`       |

userId 추출: `@AuthenticationPrincipal Long userId` (JWT 필터가 principal에 Long 세팅)

---

## 7. 관련 문서

- `docs/CONVENTION.md` — 글로벌 컨벤션 (에러코드 포맷, `ApiResponse`, Swagger 기본)
- `docs/ChatBot/clean_architecture_plan.md` — 설계 결정 17개 원본
- `docs/ChatBot/handoff.md` — Phase 진행 기록
- 동일 패턴 참조 코드:
  - `admin/operation/alert/domain/model/OperationAlert.java`
  - `admin/operation/alert/infrastructure/persistence/OperationAlertMapper.java`
  - `admin/operation/alert/application/service/OperationAlertCommandService.java`
