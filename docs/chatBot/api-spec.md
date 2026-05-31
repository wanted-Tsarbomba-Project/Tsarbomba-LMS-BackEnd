# ChatBot API 명세서

> Base URL: `/api/v1/chat`
> 인증: JWT 토큰 (Cookie 또는 Authorization Header)
> 응답 포맷: `ApiResponse<T>` 공통 래퍼

---

## 1. POST /api/v1/chat — 채팅방 생성

**기본 정보**

| 항목 | 내용 |
| --- | --- |
| API 명 | 채팅방 생성 |
| Method | POST |
| URL | /api/v1/chat |
| 인증 필요 여부 | Y |

---

**Request Header**

| type | value |
| --- | --- |
| Content-Type | application/json |
| Cookie | ACCESS_TOKEN=abc123 |

**Request Body**

| 파라미터명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| problemSetId | Long | N | 문제집 ID. null이면 자유 질문 모드 채팅방 생성 |

```json
{
  "problemSetId": 1
}
```

---

**Response Header**

| type | value |
| --- | --- |
| Content-Type | application/json |

**Response Body**

| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| timestamp | String | 응답 시각 (ISO 8601) |
| status | int | HTTP 상태코드 |
| code | String | 응답 코드 |
| message | String | 응답 메시지 |
| data | Object | |
| data.roomId | Long | 생성된 채팅방 ID |
| data.problemSetId | Long | 문제집 ID (null 가능) |
| data.problemSetTitle | String | 문제집 제목 (null 가능) |
| data.createdAt | String | 채팅방 생성일시 |
| data.updatedAt | String | 마지막 메시지 시각 (null 가능) |

**Success Example — 새 채팅방 생성 (201)**

```json
{
  "timestamp": "2026-05-22T10:00:00Z",
  "status": 201,
  "code": "CHAT-ROOM-CREATED",
  "message": "채팅방이 생성되었습니다.",
  "data": {
    "roomId": 1,
    "problemSetId": 1,
    "problemSetTitle": null,
    "createdAt": "2026-05-22T10:00:00Z",
    "updatedAt": null
  }
}
```

**Success Example — 기존 채팅방 반환 (200)**

```json
{
  "timestamp": "2026-05-22T10:00:00Z",
  "status": 200,
  "code": "CHAT-ROOM-RETRIEVED",
  "message": "채팅방 목록 조회에 성공했습니다.",
  "data": {
    "roomId": 1,
    "problemSetId": 1,
    "problemSetTitle": null,
    "createdAt": "2026-05-21T09:00:00Z",
    "updatedAt": "2026-05-22T09:30:00Z"
  }
}
```

**Status Code**

| 코드 | 상태 | code | 설명 |
| --- | --- | --- | --- |
| 201 | Created | CHAT-ROOM-CREATED | 새 채팅방 생성 |
| 200 | OK | CHAT-ROOM-RETRIEVED | 기존 채팅방 반환 |
| 401 | Unauthorized | | 인증 토큰 없거나 만료 |

**비즈니스 규칙**

| 규칙 | 설명 |
| --- | --- |
| 중복 채팅방 방지 | 동일 userId + problemSetId 채팅방 존재 시 기존 방 반환 (200) |
| 자유 질문 모드 | problemSetId = null이면 일반 채팅방 생성 |

---

## 2. GET /api/v1/chat/list — 채팅방 목록 조회

**기본 정보**

| 항목 | 내용 |
| --- | --- |
| API 명 | 채팅방 목록 조회 |
| Method | GET |
| URL | /api/v1/chat/list |
| 인증 필요 여부 | Y |

---

**Request Header**

| type | value |
| --- | --- |
| Cookie | ACCESS_TOKEN=abc123 |

**Request Parameter**: 없음 (userId는 JWT에서 추출)

---

**Response Body**

| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| timestamp | String | 응답 시각 |
| status | int | HTTP 상태코드 |
| code | String | 응답 코드 |
| message | String | 응답 메시지 |
| data | Array | 채팅방 목록 |
| data[].roomId | Long | 채팅방 ID |
| data[].problemSetId | Long | 문제집 ID (null 가능) |
| data[].problemSetTitle | String | 문제집 제목 (null 가능) |
| data[].createdAt | String | 채팅방 생성일시 |
| data[].updatedAt | String | 마지막 메시지 시각 |

**Success Example (200)**

```json
{
  "timestamp": "2026-05-22T10:00:00Z",
  "status": 200,
  "code": "CHAT-ROOM-RETRIEVED",
  "message": "채팅방 목록 조회에 성공했습니다.",
  "data": [
    {
      "roomId": 2,
      "problemSetId": 1,
      "problemSetTitle": "SQL 기초",
      "createdAt": "2026-05-22T09:00:00Z",
      "updatedAt": "2026-05-22T09:45:00Z"
    },
    {
      "roomId": 1,
      "problemSetId": null,
      "problemSetTitle": null,
      "createdAt": "2026-05-21T08:00:00Z",
      "updatedAt": "2026-05-21T08:30:00Z"
    }
  ]
}
```

**Status Code**

| 코드 | 상태 | code | 설명 |
| --- | --- | --- | --- |
| 200 | OK | CHAT-ROOM-RETRIEVED | 조회 성공 |
| 401 | Unauthorized | | 인증 토큰 없거나 만료 |

**비즈니스 규칙**

| 규칙 | 설명 |
| --- | --- |
| 본인 채팅방만 조회 | JWT에서 추출한 userId 기준 필터링 |
| 최신순 정렬 | updatedAt DESC |

---

## 3. DELETE /api/v1/chat/{roomId} — 채팅방 삭제

**기본 정보**

| 항목 | 내용 |
| --- | --- |
| API 명 | 채팅방 삭제 |
| Method | DELETE |
| URL | /api/v1/chat/{roomId} |
| 인증 필요 여부 | Y |

---

**Request Header**

| type | value |
| --- | --- |
| Cookie | ACCESS_TOKEN=abc123 |

**Path Variable**

| 파라미터명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| roomId | Long | Y | 삭제할 채팅방 ID |

---

**Response**: 없음 (204 No Content)

**Status Code**

| 코드 | 상태 | code | 설명 |
| --- | --- | --- | --- |
| 204 | No Content | | 삭제 성공 |
| 401 | Unauthorized | | 인증 토큰 없거나 만료 |
| 403 | Forbidden | CHT-002 | 본인 채팅방 아닌 경우 |
| 404 | Not Found | CHT-001 | 채팅방 없음 |

**비즈니스 규칙**

| 규칙 | 설명 |
| --- | --- |
| 소유권 검증 | 본인 채팅방이 아니면 403 반환 |

---

## 4. GET /api/v1/chat/{roomId}/messages — 채팅 내역 조회

**기본 정보**

| 항목 | 내용 |
| --- | --- |
| API 명 | 채팅 내역 조회 |
| Method | GET |
| URL | /api/v1/chat/{roomId}/messages |
| 인증 필요 여부 | Y |

---

**Request Header**

| type | value |
| --- | --- |
| Cookie | ACCESS_TOKEN=abc123 |

**Path Variable**

| 파라미터명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| roomId | Long | Y | 조회할 채팅방 ID |

---

**Response Body**

| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| timestamp | String | 응답 시각 |
| status | int | HTTP 상태코드 |
| code | String | 응답 코드 |
| message | String | 응답 메시지 |
| data | Array | 메시지 목록 |
| data[].messageId | Long | 메시지 ID |
| data[].role | String | 발신자 역할 (`USER` / `AI`) |
| data[].content | String | 메시지 내용 |
| data[].createdAt | String | 메시지 생성일시 |

**Success Example (200)**

```json
{
  "timestamp": "2026-05-22T10:00:00Z",
  "status": 200,
  "code": "CHAT-MESSAGES-RETRIEVED",
  "message": "채팅 내역 조회에 성공했습니다.",
  "data": [
    {
      "messageId": 1,
      "role": "USER",
      "content": "SELECT 문법 알려줘",
      "createdAt": "2026-05-22T09:30:00Z"
    },
    {
      "messageId": 2,
      "role": "AI",
      "content": "SELECT는 테이블에서 데이터를 조회하는 명령어입니다...",
      "createdAt": "2026-05-22T09:30:05Z"
    }
  ]
}
```

**Status Code**

| 코드 | 상태 | code | 설명 |
| --- | --- | --- | --- |
| 200 | OK | CHAT-MESSAGES-RETRIEVED | 조회 성공 |
| 401 | Unauthorized | | 인증 토큰 없거나 만료 |
| 403 | Forbidden | CHT-002 | 본인 채팅방 아닌 경우 |
| 404 | Not Found | CHT-001 | 채팅방 없음 |

**비즈니스 규칙**

| 규칙 | 설명 |
| --- | --- |
| 소유권 검증 | 본인 채팅방 아니면 403 반환 |
| 시간순 정렬 | createdAt ASC |

---

## 5. POST /api/v1/chat/{roomId}/messages — 메시지 전송

**기본 정보**

| 항목 | 내용 |
| --- | --- |
| API 명 | 메시지 전송 (AI 응답) |
| Method | POST |
| URL | /api/v1/chat/{roomId}/messages |
| 인증 필요 여부 | Y |

---

**Request Header**

| type | value |
| --- | --- |
| Content-Type | application/json |
| Cookie | ACCESS_TOKEN=abc123 |

**Path Variable**

| 파라미터명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| roomId | Long | Y | 메시지를 전송할 채팅방 ID |

**Request Body**

| 파라미터명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| problemId | Long | N | 현재 풀고 있는 문제 ID. null이면 자유 질문 모드 |
| userMessage | String | Y | 유저가 입력한 메시지 |

```json
{
  "problemId": 3,
  "userMessage": "이 문제에서 JOIN을 어떻게 써야 해?"
}
```

---

**Response Body**

| 파라미터명 | 타입 | 설명 |
| --- | --- | --- |
| timestamp | String | 응답 시각 |
| status | int | HTTP 상태코드 |
| code | String | 응답 코드 |
| message | String | 응답 메시지 |
| data | Object | |
| data.answer | String | AI가 생성한 응답 텍스트 |

**Success Example (200)**

```json
{
  "timestamp": "2026-05-22T10:00:00Z",
  "status": 200,
  "code": "CHAT-MESSAGE-SENT",
  "message": "메시지 전송에 성공했습니다.",
  "data": {
    "answer": "이 문제에서는 INNER JOIN을 사용해야 합니다. 두 테이블의 공통 키를 기준으로..."
  }
}
```

**Status Code**

| 코드 | 상태 | code | 설명 |
| --- | --- | --- | --- |
| 200 | OK | CHAT-MESSAGE-SENT | 전송 및 AI 응답 성공 |
| 401 | Unauthorized | | 인증 토큰 없거나 만료 |
| 403 | Forbidden | CHT-002 | 본인 채팅방 아닌 경우 |
| 404 | Not Found | CHT-001 | 채팅방 없음 |
| 500 | Internal Server Error | CHT-003 | FastAPI AI 응답 생성 실패 |

**비즈니스 규칙**

| 규칙 | 설명 |
| --- | --- |
| 소유권 검증 | 본인 채팅방 아니면 403 반환 |
| 유저 메시지 선저장 | FastAPI 호출 실패해도 유저 메시지는 DB에 보존 |
| 대화 히스토리 포함 | 최근 20개 메시지를 컨텍스트로 FastAPI에 전달 |
| problemId null | 자유 질문 모드 — 문제 관련 컨텍스트 없이 AI 호출 |
