# 변경사항 (2026-06-09)

## 강좌 문제세트 연결 정책

`PUT /api/v1/courses/{courseId}/problem-sets`

| role | lectureId | 의미 |
|------|-----------|------|
| `MAIN` | 필수 | 특정 강의 안에서 푸는 문제세트 |
| `FINAL` | `null` | 강좌 전체 종료 후 푸는 최종 문제세트 |

**Request 예시**

```json
{
  "problemSets": [
    {
      "lectureId": 1,
      "problemSetId": 10,
      "role": "MAIN",
      "displayOrder": 1
    },
    {
      "lectureId": null,
      "problemSetId": 99,
      "role": "FINAL",
      "displayOrder": 1
    }
  ]
}
```

**추가 에러코드**

| status | code | 설명 |
|--------|------|------|
| 400 | `CRS-014` | FINAL 문제 단계에는 강의를 지정할 수 없음 |

---

# Code-Bomba-LMS API 명세서

> 📌 이 파일은 내가 만든 **API 엔드포인트 명세**예요.
> AI는 API 코드를 작성하기 전에 이 파일을 먼저 읽어요. (명세와 다른 경로/필드를 쓰지 않도록)
> 아래 예시 양식을 복사해서 엔드포인트마다 추가하세요.

> 담당 범위: `course`, `lecture`, `enrollment`, `learning`
> Controller 기준으로 확인된 API를 정리한다.

---

## 예시 양식

> 아래 내용은 작성 예시이며, 실제 프로젝트 API 현황은 다음 섹션부터 확인한다.

## API 목록

### 1. [기능명] — 예: 회원가입

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **Path** | `/api/v1/users/signup` |
| **인증 필요** | ❌ (비로그인 허용) |

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "string",
  "nickname": "string"
}
```

**Response (201 Created)**

```json
{
  "status": 201,
  "message": "회원가입 성공",
  "data": { "userId": 1 }
}
```

**에러 코드**

| status | code | 설명 |
|--------|------|------|
| 409 | `USR-002` | 이메일 중복 |
| 400 | `USR-001` | 입력값 검증 실패 |

---

> ✏️ 위 양식을 복사해서 엔드포인트를 계속 추가하세요.
> 에러 코드 네이밍은 팀 컨벤션에 맞추세요. (예: `도메인약어-번호`)

---
## 실제 프로젝트 API 목록

> 담당 범위: `course`, `lecture`, `enrollment`, `learning`

### 1. 강좌 목록 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **Path** | `/api/v1/courses` |
| **인증 필요** | ❌ |

**Request Body**

없음

**Response (200 OK)**

```json
{
  "code": "COURSE-RETRIEVED",
  "message": "강좌를 조회했습니다.",
  "data": [
    {
      "courseId": 1,
      "title": "강좌명",
      "description": "강좌 설명",
      "thumbnailUrl": "/uploads/course-thumbnails/example.png"
    }
  ]
}
```

**에러 코드**

| status | code | 설명 |
|--------|------|------|
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

---

### 2. 강좌 상세 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **Path** | `/api/v1/courses/{courseId}` |
| **인증 필요** | ❌ |

**Request Body**

없음

**Response (200 OK)**

```json
{
  "code": "COURSE-RETRIEVED",
  "message": "강좌를 조회했습니다.",
  "data": {
    "courseId": 1,
    "title": "강좌명",
    "description": "강좌 설명",
    "thumbnailUrl": "/uploads/course-thumbnails/example.png"
  }
}
```

**에러 코드**

| status | code | 설명 |
|--------|------|------|
| 404 | `CRS-001` | 존재하지 않는 강좌 |
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

---

### 3. 강좌 생성

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **Path** | `/api/v1/courses` |
| **인증 필요** | ✅ `ROLE_OPERATOR` |

**Request Body**

```json
{
  "courseCategoryId": 1,
  "title": "강좌명",
  "description": "강좌 설명",
  "thumbnailUrl": "/uploads/course-thumbnails/example.png"
}
```

**Response (201 Created)**

```json
{
  "code": "COURSE-CREATED",
  "message": "강좌가 생성되었습니다.",
  "data": {
    "courseId": 1,
    "title": "강좌명"
  }
}
```

**에러 코드**

| status | code | 설명 |
|--------|------|------|
| 400 | `COMMON-BAD-REQUEST` | 입력값 검증 실패 |
| 400 | `CRS-006` | 강좌는 운영자만 생성 가능 |
| 400 | `CRS-007` | 활성화된 강좌 카테고리 필요 |
| 401 | `AUT-016` | 인증 필요 |
| 403 | `AUT-015` | 접근 권한 없음 |
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

---

### 4. 강좌 수정 / 공개 / 삭제

| 항목 | 내용 |
|------|------|
| **Method** | `PUT`, `PATCH`, `DELETE` |
| **Path** | `/api/v1/courses/{courseId}`, `/api/v1/courses/{courseId}/publish` |
| **인증 필요** | ✅ `ROLE_OPERATOR` |

**Request Body**

```json
{
  "courseCategoryId": 1,
  "title": "수정할 강좌명",
  "description": "수정할 강좌 설명",
  "thumbnailUrl": "/uploads/course-thumbnails/example.png",
  "status": "ACTIVE"
}
```

**Response**

```json
{
  "code": "COURSE-UPDATED",
  "message": "강좌가 수정되었습니다.",
  "data": {
    "courseId": 1,
    "title": "수정할 강좌명"
  }
}
```

**에러 코드**

| status | code | 설명 |
|--------|------|------|
| 400 | `COMMON-BAD-REQUEST` | 입력값 검증 실패 |
| 400 | `CRS-002` | 강좌 개설 시 강의가 1개 이상 필요 |
| 400 | `CRS-003` | 작성 중인 강좌 활성화는 개설 기능으로만 가능 |
| 400 | `CRS-004` | 작성 중인 강좌만 개설 가능 |
| 400 | `CRS-005` | 강좌 삭제는 삭제 기능으로만 가능 |
| 404 | `CRS-001` | 존재하지 않는 강좌 |
| 401 | `AUT-016` | 인증 필요 |
| 403 | `AUT-015` | 접근 권한 없음 |
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

---

### 5. 강좌 썸네일 업로드

| 항목 | 내용 |
|------|------|
| **Method** | `POST` |
| **Path** | `/api/v1/courses/thumbnails` |
| **인증 필요** | ✅ `ROLE_OPERATOR` |

**Request Body**

`multipart/form-data`

| 필드 | 타입 | 설명 |
|------|------|------|
| `thumbnail` | file | 업로드할 이미지 파일 |

**Response (201 Created)**

```json
{
  "code": "COURSE-THUMBNAIL-UPLOADED",
  "message": "Course thumbnail has been uploaded.",
  "data": {
    "thumbnailUrl": "/uploads/course-thumbnails/example.png"
  }
}
```

**에러 코드**

| status | code | 설명 |
|--------|------|------|
| 400 | `COMMON-BAD-REQUEST` | 파일 없음, 이미지 파일 아님, 잘못된 요청 형식 |
| 401 | `AUT-016` | 인증 필요 |
| 403 | `AUT-015` | 접근 권한 없음 |
| 500 | `INTERNAL_ERROR` | 파일 저장 실패 또는 서버 내부 오류 |

---

### 6. 강좌 문제세트 연결 조회 / 저장

| 항목 | 내용 |
|------|------|
| **Method** | `GET`, `PUT` |
| **Path** | `/api/v1/courses/{courseId}/problem-sets`, `/api/v1/lectures/{lectureId}/problem-sets` |
| **인증 필요** | 조회 ❌ / 저장 ✅ `ROLE_OPERATOR` |

**Request Body**

```json
{
  "problemSets": [
    {
      "lectureId": 1,
      "problemSetId": 1,
      "role": "PRACTICE",
      "displayOrder": 1
    }
  ]
}
```

**Response (200 OK)**

```json
{
  "code": "COURSE-UPDATED",
  "message": "강좌 정보가 수정되었습니다.",
  "data": [
    {
      "lectureId": 1,
      "problemSetId": 1,
      "role": "PRACTICE",
      "displayOrder": 1
    }
  ]
}
```

**에러 코드**

| status | code | 설명 |
|--------|------|------|
| 400 | `COMMON-BAD-REQUEST` | 입력값 검증 실패 |
| 400 | `CRS-008` | 강좌에 연결할 문제세트 필요 |
| 400 | `CRS-009` | 강좌 문제 연결 단계 정보 필요 |
| 400 | `CRS-010` | 존재하지 않는 문제세트 |
| 400 | `CRS-011` | 선택한 문제세트에 존재하지 않는 문제 |
| 400 | `CRS-012` | MAIN 문제 단계에는 강의 필요 |
| 400 | `CRS-013` | 선택한 강좌에 존재하지 않는 강의 |
| 404 | `CRS-001` | 존재하지 않는 강좌 |
| 401 | `AUT-016` | 인증 필요 |
| 403 | `AUT-015` | 접근 권한 없음 |
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

---

### 7. 강의 목록 / 상세 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **Path** | `/api/v1/courses/{courseId}/lectures`, `/api/v1/lectures/{lectureId}` |
| **인증 필요** | ❌ |

**Request Body**

없음

**Response (200 OK)**

```json
{
  "code": "LECTURE-RETRIEVED",
  "message": "강의를 조회했습니다.",
  "data": {
    "lectureId": 1,
    "title": "강의명",
    "description": "강의 설명",
    "videoUrl": "https://example.com/video",
    "thumbnailUrl": "https://example.com/thumb.png",
    "lectureOrder": 1
  }
}
```

**에러 코드**

| status | code | 설명 |
|--------|------|------|
| 404 | `CRS-001` | 존재하지 않는 강좌 |
| 404 | `LCT-001` | 존재하지 않는 강의 |
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

---

### 8. 강의 생성 / 수정 / 삭제

| 항목 | 내용 |
|------|------|
| **Method** | `POST`, `PUT`, `DELETE` |
| **Path** | `/api/v1/courses/{courseId}/lectures`, `/api/v1/lectures/{lectureId}` |
| **인증 필요** | ✅ `ROLE_OPERATOR` |

**Request Body**

```json
{
  "title": "강의명",
  "description": "강의 설명",
  "videoUrl": "https://example.com/video",
  "thumbnailUrl": "https://example.com/thumb.png",
  "lectureOrder": 1,
  "status": "ACTIVE"
}
```

**Response**

```json
{
  "code": "LECTURE-CREATED",
  "message": "강의가 생성되었습니다.",
  "data": {
    "lectureId": 1,
    "title": "강의명"
  }
}
```

**에러 코드**

| status | code | 설명 |
|--------|------|------|
| 400 | `COMMON-BAD-REQUEST` | 입력값 검증 실패 |
| 400 | `LCT-002` | 강의 삭제는 삭제 API 사용 필요 |
| 404 | `CRS-001` | 존재하지 않는 강좌 |
| 404 | `LCT-001` | 존재하지 않는 강의 |
| 409 | `LCT-003` | 같은 순서의 강의가 이미 존재 |
| 401 | `AUT-016` | 인증 필요 |
| 403 | `AUT-015` | 접근 권한 없음 |
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

---

### 9. 수강신청 생성 / 조회 / 취소

| 항목 | 내용 |
|------|------|
| **Method** | `POST`, `GET`, `DELETE` |
| **Path** | `/api/v1/courses/{courseId}/enrollments`, `/api/v1/users/me/enrollments`, `/api/v1/users/me/enrollments/{enrollmentId}`, `/api/v1/users/{userId}/enrollments`, `/api/v1/enrollments`, `/api/v1/users/{userId}/enrollments/{enrollmentId}` |
| **인증 필요** | ✅ 수강신청/내 목록/내 취소 `ROLE_STUDENT`, 전체 조회 `ROLE_ADMIN` 또는 `ROLE_OPERATOR` |

**Request Body**

없음

**Response**

```json
{
  "code": "ENROLLMENT-CREATED",
  "message": "수강신청이 완료되었습니다.",
  "data": {
    "enrollmentId": 1,
    "courseId": 1,
    "userId": 1
  }
}
```

**에러 코드**

| status | code | 설명 |
|--------|------|------|
| 400 | `ENR-003` | 이미 취소된 수강 신청 |
| 400 | `ENR-004` | 개설된 강좌만 수강 신청 가능 |
| 400 | `ENR-005` | 학생만 수강 신청 가능 |
| 404 | `CRS-001` | 존재하지 않는 강좌 |
| 404 | `ENR-001` | 존재하지 않는 수강 신청 |
| 409 | `ENR-002` | 이미 수강 신청한 강좌 |
| 401 | `AUT-016` | 인증 필요 |
| 403 | `AUT-015` | 접근 권한 없음 |
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

---

### 10. 강의 / 문제세트 학습 진행률

| 항목 | 내용 |
|------|------|
| **Method** | `GET`, `PATCH` |
| **Path** | `/api/v1/lectures/{lectureId}/progress`, `/api/v1/lecture-problem-sets/{lectureProblemSetId}/progress` |
| **인증 필요** | ✅ |

**Request Body**

```json
{
  "completed": true
}
```

```json
{
  "currentProblemNumber": 1,
  "completed": false
}
```

**Response (200 OK)**

```json
{
  "code": "LEARNING-UPDATED",
  "message": "학습 진행률이 저장되었습니다.",
  "data": {
    "completed": true
  }
}
```

**에러 코드**

| status | code | 설명 |
|--------|------|------|
| 400 | `COMMON-BAD-REQUEST` | 입력값 검증 실패 |
| 404 | `LRN-001` | 존재하지 않는 강의 |
| 404 | `LRN-003` | 학습 진행률 없음 |
| 404 | `LRN-005` | 강의 문제세트 없음 |
| 401 | `AUT-016` | 인증 필요 |
| 403 | `AUT-015` | 접근 권한 없음 |
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

---

### 11. 강의 문제세트 입장 / 제출

| 항목 | 내용 |
|------|------|
| **Method** | `GET`, `POST` |
| **Path** | `/api/v1/lecture-problem-sets/{lectureProblemSetId}`, `/api/v1/lecture-problem-sets/{lectureProblemSetId}/problems/{problemId}/submissions` |
| **인증 필요** | ✅ |

**Request Body**

```json
{
  "code": "print('hello')"
}
```

**Response (200 OK)**

```json
{
  "code": "LEARNING-SUBMITTED",
  "message": "문제가 제출되었습니다.",
  "data": {
    "submissionId": 1,
    "correct": true
  }
}
```

**에러 코드**

| status | code | 설명 |
|--------|------|------|
| 400 | `COMMON-BAD-REQUEST` | 입력값 검증 실패 |
| 404 | `LRN-005` | 강의 문제세트 없음 |
| 404 | `LRN-006` | 문제가 해당 강의 문제세트에 속하지 않음 |
| 404 | `LRN-007` | 존재하지 않는 문제 |
| 400 | `LRN-008` | 아직 열리지 않은 강의 문제 제출 |
| 409 | `LRN-009` | 이미 완료한 강의 문제세트 제출 |
| 401 | `AUT-016` | 인증 필요 |
| 403 | `AUT-015` | 접근 권한 없음 |
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

---

### 12. 관리자용 학습률 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **Path** | `/api/v1/courses/{courseId}/learning-progress`, `/api/v1/courses/learning-progress`, `/api/v1/courses/{courseId}/users/learning-progress`, `/api/v1/courses/{courseId}/users/{userId}/learning-progress`, `/api/v1/courses/{courseId}/lectures/learning-progress`, `/api/v1/lectures/{lectureId}/problems/statistics`, `/api/v1/learning-progress/summary` |
| **인증 필요** | ✅ `ROLE_ADMIN` 또는 `ROLE_OPERATOR` |

**Request Body**

없음

**Response (200 OK)**

```json
{
  "code": "LEARNING-RETRIEVED",
  "message": "학습률을 조회했습니다.",
  "data": {
    "courseId": 1,
    "progressRate": 75.0
  }
}
```

**에러 코드**

| status | code | 설명 |
|--------|------|------|
| 404 | `LRN-001` | 존재하지 않는 강의 |
| 404 | `LRN-002` | 강좌 문제 단계 없음 |
| 404 | `LRN-003` | 학습 진행률 없음 |
| 404 | `LRN-004` | 존재하지 않는 강좌 |
| 404 | `LRN-005` | 강의 문제세트 없음 |
| 401 | `AUT-016` | 인증 필요 |
| 403 | `AUT-015` | 접근 권한 없음 |
| 500 | `INTERNAL_ERROR` | 서버 내부 오류 |

---

## 미완료 / 확인 필요 API

| 항목 | 상태 | 비고 |
|------|------|------|
| API별 정확한 에러 코드 | 일부 확인 필요 | 도메인 ErrorCode 기준으로 반영했으나 API별 실제 발생 여부는 테스트로 추가 확인 필요 |
| Response 상세 필드 | 확인 필요 | DTO 기준으로 상세 응답 예시 보완 필요 |
| 프론트 연동 여부 | 확인 필요 | API 구현은 확인됐지만 화면별 연동 완료 여부는 별도 확인 필요 |
