# Chatbot Domain

`chatbot` 도메인은 학습자가 문제 풀이 중 AI 챗봇과 대화할 수 있도록 채팅방, 메시지, AI 응답 생성 흐름을 관리한다. Spring 애플리케이션 내부의 채팅 기록과 외부 FastAPI 기반 AI 서버 연동을 함께 담당한다.

## 주요 역할

- 첫 메시지를 전송하면서 새 채팅방을 생성한다.
- 기존 채팅방에 사용자 메시지를 추가하고 AI 응답을 저장한다.
- 사용자의 채팅방 목록과 채팅방별 메시지 이력을 조회한다.
- 채팅방을 삭제 처리한다.
- 문제 세트, 문제, 진행률, 이전 대화 이력을 조합해 AI 요청 컨텍스트를 만든다.

## 패키지 구조

```text
chatbot
├── application
│   ├── command      # 메시지 전송, 채팅방 생성/삭제 명령
│   ├── port         # AI 클라이언트와 학습 컨텍스트 조회 포트
│   ├── query        # 채팅방/메시지 조회 조건
│   ├── service      # 채팅방, 메시지, 컨텍스트 유스케이스 구현
│   └── usecase      # presentation 계층 입력 포트
├── domain
│   ├── model        # ChatRoom, ChatMessage, MessageRole
│   └── repository   # 채팅방/메시지 저장소 인터페이스
├── infrastructure
│   ├── adapter      # 문제/학습 컨텍스트 어댑터
│   ├── client       # FastAPI, Mock AI 클라이언트
│   └── persistence  # JPA 엔티티, mapper, repository adapter
└── presentation
    └── api          # REST Controller, request/response DTO
```

## 주요 모델

| 모델 | 설명 |
| --- | --- |
| `ChatRoom` | 사용자와 문제 세트 기준으로 생성되는 대화방 |
| `ChatMessage` | 채팅방에 저장되는 사용자/AI 메시지 |
| `MessageRole` | 메시지 작성 주체를 구분하는 역할 값 |

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `ChatRoomCommandService` | 첫 메시지 기반 채팅방 생성, 채팅방 삭제 |
| `ChatRoomQueryService` | 사용자 채팅방 목록 조회 |
| `ChatMessageCommandService` | 메시지 전송, AI 응답 요청, 메시지 저장 |
| `ChatMessageQueryService` | 채팅방 메시지 이력 조회 |
| `ChatContextBuilder` | AI 요청에 필요한 학습/문제/진행률 컨텍스트 구성 |

## API 목록

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/v1/chat/messages` | 첫 메시지 전송 및 채팅방 생성 |
| `GET` | `/api/v1/chat/list` | 내 채팅방 목록 조회 |
| `POST` | `/api/v1/chat/{roomId}/messages` | 기존 채팅방에 메시지 전송 |
| `GET` | `/api/v1/chat/{roomId}/messages` | 채팅방 메시지 목록 조회 |
| `DELETE` | `/api/v1/chat/{roomId}` | 채팅방 삭제 |

## 핵심 흐름

### 첫 메시지 전송

1. 사용자가 문제 세트와 메시지를 전달한다.
2. 채팅방을 생성하고 사용자 메시지를 저장한다.
3. `ChatContextBuilder`가 문제 세트, 문제 목록, 학습 진행률, 이전 대화 맥락을 구성한다.
4. `AiChatClient` 구현체가 외부 AI 서버 또는 Mock 클라이언트로 응답을 요청한다.
5. AI 응답을 메시지로 저장하고 클라이언트에 반환한다.

### 기존 채팅방 메시지 전송

1. 채팅방 존재 여부와 접근 가능한 사용자인지 확인한다.
2. 사용자 메시지를 저장한다.
3. 채팅방과 연결된 학습 컨텍스트를 다시 구성한다.
4. AI 응답을 저장한 뒤 사용자 메시지/AI 메시지 결과를 반환한다.

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `problems` | 문제 세트와 문제 정보를 AI 컨텍스트에 포함 |
| `learning` | 사용자의 문제 풀이 진행률과 학습 상태를 컨텍스트에 포함 |
| `user` | 사용자 ID 기준으로 채팅방과 메시지 접근 범위 제한 |

## 참고 문서

- `api-spec.md`: chatbot API 상세 명세
- `clean_architecture_plan.md`: chatbot 도메인 클린 아키텍처 전환 계획
- `convention.md`: chatbot 도메인 작업 컨벤션
- `fastapi_project_convention.md`: FastAPI 연동 프로젝트 컨벤션
- `handoff.md`: 인수인계 문서
