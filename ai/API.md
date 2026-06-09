# Code-Bomba-LMS 담당 도메인 API 명세

> 배정현 담당 도메인의 실제 Controller 매핑과 구현 상태를 기준으로 정리한다.

## 상태 표기

| 상태 | 의미 |
|------|------|
| ✅ 구현 | API 경로부터 서비스/저장소 로직까지 동작 |
| 🚧 스텁 | API 경로는 있지만 실제 비즈니스/저장 로직 미구현 |
| 📝 예정 | 구현 전 명세 초안이며 프론트/타 도메인 협의 후 변경 가능 |

---

## Problem - 조회 및 문제풀이

| 기능 | Method | Path | 인증/권한 | 상태 |
|------|--------|------|-----------|------|
| 활성 카테고리 조회 | `GET` | `/api/v1/problem-categories` | 프로젝트 보안 정책 적용 | ✅ |
| 문제 세트 목록 조회 | `GET` | `/api/v1/problem-sets` | 프로젝트 보안 정책 적용 | ✅ |
| 문제 세트 진입 및 전체 소문제/진행 상태 조회 | `GET` | `/api/v1/problem-sets/{problemSetId}` | 로그인 | ✅ |
| 문제 세트 진행 상태 조회 | `GET` | `/api/v1/problem-sets/{problemSetId}/progress` | 로그인 사용자 기준 | ✅ |
| 완료 문제 세트 결과 조회 | `GET` | `/api/v1/problem-sets/{problemSetId}/result` | 로그인 사용자 기준 | ✅ |
| 문제 힌트 조회 | `GET` | `/api/v1/problems/{problemId}/hints` | 프로젝트 보안 정책 적용 | ✅ |

---

## Problem - Course/Lecture 연동 (M4 초안)

> 2026-06-08 기준 프론트 협의용 초안이다. Course/Lecture 담당 도메인의 기존 API, 권한 정책, DTO와 맞추면서 경로와 응답 필드는 변경될 수 있다.

| 기능 | Method | Path | 인증/권한 | 상태 |
|------|--------|------|-----------|------|
| 문제 연관 강의 조회 | `GET` | `/api/v1/problems/{id}/lectures` | 로그인 학생 | 📝 예정 |
| 강좌-문제세트 연결 | `POST` | `/api/v1/course-problem-sets` | 강의/문제 운영자 | 📝 예정 |
| 문제세트 연관 강좌 조회 | `GET` | `/api/v1/problem-sets/{problemSetId}/courses` | 로그인 학생 | 📝 예정 |

### 경로 후보 메모

| 후보 | 용도 | 상태 |
|------|------|------|
| `POST /api/v1/problem-lecture-mappings` | 문제와 강의를 직접 연결하는 API 후보 | 상세 명세 확정 필요 |
| `POST /api/v1/course-problem-sets` | 강좌와 문제 세트를 연결하는 API 후보 | 현재 우선 초안 |

### GET `/api/v1/problems/{id}/lectures` - 문제 연관 강의 조회

문제 ID 기준으로 연결된 강의 목록을 조회한다. 오답 이후 추천 강의 제공 흐름에 사용할 수 있다.

#### Path Parameter

| 파라미터명 | 타입 | 필수 여부 | 설명 |
|------------|------|-----------|------|
| `id` | Long | Y | 문제 ID |

#### Response Parameter

| 파라미터명 | 타입 | 설명 |
|------------|------|------|
| `httpStatus` | int | HTTP 상태코드 |
| `message` | String | 응답 메시지 |
| `data` | Array | 강의 목록 |
| `lectureId` | Long | 강의 ID |
| `courseId` | Long | 강좌 ID |
| `title` | String | 강의 제목 |
| `description` | String | 강의 설명 |
| `videoUrl` | String | 강의 영상 URL |
| `thumbnailUrl` | String | 썸네일 이미지 URL |

#### Success Example

```json
{
  "httpStatus": 200,
  "message": "문제 연관 강의 조회 성공",
  "data": [
    {
      "lectureId": 3,
      "courseId": 1,
      "title": "SQL 기본 문법",
      "description": "SELECT 문법을 배우는 강의입니다.",
      "videoUrl": "https://example.com/video/3",
      "thumbnailUrl": "https://example.com/images/lecture3.jpg"
    },
    {
      "lectureId": 4,
      "courseId": 1,
      "title": "WHERE 조건문",
      "description": "조건 필터링 방법을 배우는 강의입니다.",
      "videoUrl": "https://example.com/video/4",
      "thumbnailUrl": "https://example.com/images/lecture4.jpg"
    }
  ]
}
```

#### Status Code

| 코드 | 상태 | code 후보 | 설명 |
|------|------|-----------|------|
| 200 | OK | - | 조회 성공 |
| 401 | Unauthorized | `AUTH_TOKEN_EXPIRED` | 인증 토큰 만료 |
| 403 | Forbidden | `PROBLEM_ACCESS_DENIED` | 문제 접근 권한 없음 |
| 404 | Not Found | `PROBLEM_NOT_FOUND` | 문제 없음 |

#### 비즈니스 규칙

| 규칙 | 설명 |
|------|------|
| 문제 기준 조회 | `problem_id` 기준으로 연결된 강의를 반환 |
| 다대다 관계 | `problem_lecture` 테이블 기반 조회 후보 |
| 접근 조건 | 해당 문제에 접근 가능한 사용자만 조회 가능 |
| 학습 유도 | 오답 시 추천 강의 제공 흐름에 사용 가능 |
| 정렬 기준 | 추천 우선순위 또는 등록 순 |

### POST `/api/v1/course-problem-sets` - 강좌-문제세트 연결

운영자가 강좌와 문제 세트를 연결한다.

#### Request Body

| 파라미터명 | 타입 | 필수 여부 | 설명 |
|------------|------|-----------|------|
| `courseId` | Long | Y | 강좌 ID |
| `problemSetId` | Long | Y | 문제 세트 ID |

#### Success Example

```json
{
  "httpStatus": 201,
  "message": "강좌-문제세트 연결 성공",
  "data": {
    "courseProblemSetId": 1,
    "courseId": 3,
    "problemSetId": 10
  }
}
```

#### Status Code

| 코드 | 상태 | code 후보 | 설명 |
|------|------|-----------|------|
| 201 | Created | - | 연결 성공 |
| 400 | Bad Request | `COURSE_PROBLEM_SET_INVALID_INPUT` | 입력값 오류 |
| 403 | Forbidden | `COURSE_PROBLEM_SET_ACCESS_DENIED` | 권한 없음 |
| 404 | Not Found | `COURSE_NOT_FOUND` | 강좌 없음 |
| 404 | Not Found | `PROBLEM_SET_NOT_FOUND` | 문제 세트 없음 |
| 409 | Conflict | `COURSE_PROBLEM_SET_ALREADY_EXISTS` | 이미 연결됨 |

### GET `/api/v1/problem-sets/{problemSetId}/courses` - 문제세트 연관 강좌 조회

문제 세트 ID 기준으로 연결된 강좌 목록을 조회한다.

#### Path Parameter

| 파라미터명 | 타입 | 필수 여부 | 설명 |
|------------|------|-----------|------|
| `problemSetId` | Long | Y | 문제 세트 ID |

#### Success Example

```json
{
  "httpStatus": 200,
  "message": "연관 강좌 조회 성공",
  "data": [
    {
      "courseId": 3,
      "title": "비즈니스 모델 입문",
      "description": "비즈니스 모델과 고객 세그먼트를 이해하는 강좌입니다.",
      "thumbnailUrl": "/images/courses/business-basic.png"
    }
  ]
}
```

#### Status Code

| 코드 | 상태 | code 후보 | 설명 |
|------|------|-----------|------|
| 200 | OK | - | 조회 성공 |
| 403 | Forbidden | `COURSE_PROBLEM_SET_ACCESS_DENIED` | 권한 없음 |
| 404 | Not Found | `PROBLEM_SET_NOT_FOUND` | 문제 세트 없음 |

---

## Problem - 관리자 문제/데이터셋/테스트케이스 관리

| 기능 | Method | Path | 인증/권한 | 상태 |
|------|--------|------|-----------|------|
| 문제 세트 등록 | `POST` | `/api/v1/problems` | ADMIN / OPERATOR | ✅ |
| 문제 세트 수정 | `PUT` | `/api/v1/problems/{problemSetId}` | ADMIN / OPERATOR | ✅ |
| 문제 세트 삭제 | `DELETE` | `/api/v1/problems/{problemSetId}?force=false` | ADMIN / OPERATOR | ✅ |
| 수정용 문제 세트 상세 조회 | `GET` | `/api/v1/problems/{problemSetId}` | ADMIN / OPERATOR | ✅ |
| 문제 세트 + CSV 동시 등록 | `POST` | `/api/v1/problems/with-dataset` | ADMIN / OPERATOR | ✅ |
| 문제 세트 + CSV 동시 수정 | `PUT` | `/api/v1/problems/{problemSetId}/with-dataset` | ADMIN / OPERATOR | ✅ |
| CSV 데이터셋 업로드 | `POST` | `/api/v1/problem-datasets` | ADMIN / OPERATOR | ✅ |
| 테스트케이스 등록 | `POST` | `/api/v1/problems/{problemId}/test-cases` | ADMIN / OPERATOR | ✅ |
| 테스트케이스 목록 조회 | `GET` | `/api/v1/problems/{problemId}/test-cases` | ADMIN / OPERATOR | ✅ |
| 테스트케이스 수정 | `PUT` | `/api/v1/test-cases/{testCaseId}` | ADMIN / OPERATOR | ✅ |
| 테스트케이스 삭제 | `DELETE` | `/api/v1/test-cases/{testCaseId}` | ADMIN / OPERATOR | ✅ |

### 문제 세트 + CSV 동시 등록 요청

- `Content-Type`: `multipart/form-data`
- `request`: 문제 세트와 소문제 정보 JSON 파트
- `datasetFile`: CSV 파일 파트

---

## Execution / Submission

| 기능 | Method | Path | 인증/권한 | 상태 |
|------|--------|------|-----------|------|
| 코드 실행 | `POST` | `/api/v1/code-problems/{problemId}/executions` | 프로젝트 보안 정책 적용 | ✅ |
| 코드 제출 및 채점 | `POST` | `/api/v1/problems/{problemId}/submissions` | 로그인 | ✅ |
| 제출 채점 결과 조회 | `GET` | `/api/v1/code-submissions/{submissionId}` | 로그인, 제출 소유자 기준 | ✅ |
| 제출 채점 결과 조회 별칭 | `GET` | `/api/v1/submissions/{submissionId}/result` | 로그인, 제출 소유자 기준 | ✅ |
| 문제별 내 제출 기록 조회 | `GET` | `/api/v1/code-problems/{problemId}/submissions?page=1&size=10` | 로그인 | ✅ |
| 관리자 학생 풀이 기록 조회 | `GET` | `/api/v1/admin/students/{userId}/problems` | ADMIN | ✅ |

---

## Ranking

| 기능 | Method | Path | 인증/권한 | 상태 |
|------|--------|------|-----------|------|
| 누적 포인트 랭킹 조회 | `GET` | `/api/v1/rankings/points?page=0&size=20` | 프로젝트 보안 정책 적용 | ✅ |
| 주간 포인트 랭킹 조회 | `GET` | `/api/v1/rankings/points/weekly?page=0&size=20` | 프로젝트 보안 정책 적용 | ✅ |
| 내 포인트 랭킹 조회 | `GET` | `/api/v1/rankings/points/me` | 로그인 사용자 기준 | ✅ |

---

## Reward Point

포인트 지급은 외부 공개 Controller가 아니라 정답 제출 이후 이벤트 처리로 동작한다.

| 흐름 | 트리거 | 처리 | 상태 |
|------|--------|------|------|
| 문제 정답 포인트 지급 | 정답 제출 트랜잭션 커밋 완료 | 사용자 누적 포인트 갱신 및 이력 저장 | ✅ |
| 중복 지급 방지 | 동일 사용자/문제 재지급 시도 | 중복 지급 예외 처리 | ✅ |

---

## GCS 보안 강화 - M4 예정

현재 문제 데이터셋과 향후 뱃지 이미지는 GCS 객체 URL을 사용한다. M4에서는 공개 URL 의존을 줄이고, 접근 제어가 가능한 방식으로 보강한다.

| 항목 | 현재 | 보강 후보 |
|------|------|-----------|
| CSV 데이터셋 | 공개 URL 기반 시작 코드 연동 | Private bucket + Signed URL 또는 Python Runner 인증 접근 |
| 뱃지 이미지 | GCS 이미지 저장 어댑터 미구현 | Private bucket + Signed URL 또는 인증 프록시 |
| API 응답 | URL 문자열 반환 | `url`, `expiresAt` 또는 `fileKey` 포함 여부 검토 |
| 보상 삭제 | DB 저장 실패 시 GCS 객체 삭제 | Signed URL 생성 실패/객체 삭제 실패 예외 정책 추가 |

확정 전까지 신규 이미지/파일 URL 응답은 보안 방식에 따라 변경될 수 있다.

---

## Badge - M4 구현 상태

사진에 정의된 뱃지 API 5개는 모두 Controller 경로가 존재하지만, 실제 서비스와 저장소 로직은 아직 구현되지 않았다.

| 기능 | Method | Path | 인증/권한 | 경로 | 실제 기능 |
|------|--------|------|-----------|------|-----------|
| 뱃지 조건 등록 | `POST` | `/api/v1/admin/badges` | ADMIN / OPERATOR | 존재 | 🚧 스텁 |
| 뱃지 조건 수정 | `PATCH` | `/api/v1/admin/badges/{badgeId}` | ADMIN / OPERATOR | 존재 | 🚧 스텁 |
| 뱃지 조건 삭제 | `DELETE` | `/api/v1/admin/badges/{badgeId}` | ADMIN / OPERATOR | 존재 | 🚧 스텁 |
| 내 뱃지 조회 | `GET` | `/api/v1/badges/me` | 로그인 사용자 기준 | 존재 | 🚧 빈 목록 반환 |
| 포인트 기준 뱃지 동기화 | `POST` | `/api/v1/badges/me/sync` | 로그인 사용자 기준 | 존재 | 🚧 0건 반환 |

### 추가로 존재하는 뱃지 경로

| 기능 | Method | Path | 상태 |
|------|--------|------|------|
| 관리자 뱃지 목록 조회 | `GET` | `/api/v1/admin/badges` | 🚧 빈 목록 반환 |
| 관리자 뱃지 상세 조회 | `GET` | `/api/v1/admin/badges/{badgeId}` | 🚧 `null` 반환으로 오류 가능 |
| 대표 뱃지 설정 | `PATCH` | `/api/v1/badges/me/{badgeId}/equip` | 🚧 `null` 반환으로 오류 가능 |

---

## 주요 에러 코드

### Problem

| code | 설명 |
|------|------|
| `PRB-PBL-001` | 문제를 찾을 수 없음 |
| `PRB-PBL-002` | 문제 접근 권한 없음 |
| `PRB-PBL-003` | 이전 문제를 먼저 풀어야 함 |
| `PRB-PBL-004` | 제출 기록이 존재하여 삭제 불가 |
| `PRB-SET-001` | 문제 세트를 찾을 수 없음 |
| `PRB-SET-002` | 제출 가능 횟수 초과 |
| `PRB-SET-004` | 문제 세트를 끝까지 풀지 않음 |
| `PRB-CAT-001` | 문제 분야를 찾을 수 없음 |
| `PRB-DAT-004` | CSV 파일이 아니거나 파일 검증 실패 |
| `PRB-DAT-005` | 데이터셋 업로드 실패 |
| `PRB-TC-001` | 테스트케이스를 찾을 수 없음 |
| `PRB-TC-004` | 코드 문제가 아닌 문제에 테스트케이스 등록 시도 |
| `PRB-TC-006` | 동일 순서 테스트케이스 중복 |
| `PRB-EXE-001` | 실행할 코드가 비어 있음 |
| `PRB-EXE-002` | 코드 실행 실패 |

### Course / Lecture 연동 후보

| code 후보 | 설명 |
|-----------|------|
| `COURSE_PROBLEM_SET_INVALID_INPUT` | 강좌-문제세트 연결 요청 입력값 오류 |
| `COURSE_PROBLEM_SET_ACCESS_DENIED` | 강좌-문제세트 연결 또는 조회 권한 없음 |
| `COURSE_PROBLEM_SET_ALREADY_EXISTS` | 이미 연결된 강좌-문제세트 |
| `COURSE_NOT_FOUND` | 강좌를 찾을 수 없음 |
| `PROBLEM_SET_NOT_FOUND` | 문제 세트를 찾을 수 없음 |
| `PROBLEM_NOT_FOUND` | 문제를 찾을 수 없음 |
| `PROBLEM_ACCESS_DENIED` | 문제 접근 권한 없음 |

### Submission / Reward / Ranking

| code | 설명 |
|------|------|
| `SUB-001` | 코드 값이 비어 있음 |
| `SUB-003` | 재시도할 수 없는 문제 |
| `SUB-004` | 제출 가능한 횟수 초과 |
| `SUB-005` | 제출 기록을 찾을 수 없음 |
| `RWD-PNT-001` | 이미 포인트가 지급된 문제 |
| `RWD-PNT-002` | 포인트 지급 실패 |
| `RNK-001` | 랭킹 정보를 찾을 수 없음 |

### Badge

`BadgeErrorCode`는 현재 빈 클래스이며 M4에서 정의해야 한다.

권장 후보:

| code 후보 | 설명 |
|-----------|------|
| `BDG-001` | 뱃지를 찾을 수 없음 |
| `BDG-002` | 이미 획득한 뱃지 |
| `BDG-003` | 획득하지 않은 뱃지를 대표 뱃지로 설정 |
| `BDG-004` | 뱃지 이미지 업로드 실패 |
| `BDG-005` | 뱃지 지급 조건이 올바르지 않음 |
## 2026-06-09 문제세트·테스트케이스 일괄 관리

- `POST /api/v1/problems`: 각 `problems[]`에 `testCases[]`를 포함해 문제와 테스트케이스를 한 트랜잭션에서 등록한다.
- `POST /api/v1/problems/with-dataset`: 일반 등록과 동일하게 각 `problems[].testCases[]`를 등록하며, 응답에 `createdTestCaseCount`를 포함한다.
  - Content-Type: `multipart/form-data`
  - `request`: Content-Type `application/json`, `ProblemSetCreateRequest` 형식. 각 `problems[].testCases[]` 포함
  - `datasetFile`: Content-Type `text/csv`, 필수 CSV 파일 파트
  - 성공 응답: 생성된 문제세트·데이터셋 ID, 문제·테스트케이스 생성 수, 데이터셋 참조 URL, 시작 코드 반환
- `PUT /api/v1/problems/{problemSetId}`: `testCases[]`를 기준으로 기존 테스트케이스 수정, 신규 생성, 누락된 활성 테스트케이스 비활성화를 처리한다.
- `GET /api/v1/problems/{problemSetId}`: 운영자 수정 화면을 위해 각 문제에 활성 테스트케이스 목록을 포함한다.
- `PUT /api/v1/problems/{problemSetId}/with-dataset`: `multipart/form-data`의 `request` JSON 파트와 선택적 `datasetFile` CSV 파트를 받는다.
- 테스트 순서는 배열 순서로 자동 지정하며, `timeoutMs` 생략 시 `3000ms`를 적용한다.
- 문제 등록·수정·조회 API에서 `answer`를 제거했다. 실제 채점은 `testCases[].testCode`의 성공 여부를 사용한다.
- 테스트 케이스 API에서 `expectedResult`를 제거했다. 테스트 검증식과 기대 조건은 `testCode` 안에 작성한다.
- 문제세트 수정에서 `problems[]`에 빠진 기존 문제와 해당 활성 테스트케이스는 `INACTIVE` 처리한다.
- 문제세트 삭제 시 모든 활성 문제와 해당 활성 테스트케이스를 `INACTIVE` 처리한다. 힌트 기록은 보존하지만 활성 문제의 힌트만 조회한다.

---
