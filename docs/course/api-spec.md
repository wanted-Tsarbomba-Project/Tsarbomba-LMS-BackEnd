# Course API 명세

## 1. 문서 목적

이 문서는 Code-Bomba-LMS 프로젝트의 Course 도메인 API를 정리한 문서입니다.

Course 도메인은 강좌 생성, 조회, 수정, 공개, 삭제와 강좌 카테고리 조회, 강좌-문제세트 연결 API를 제공합니다.

---

## 2. 공통 정보

### Base URL

```text
/api/v1
```

### 공통 응답 형식

Course API는 공통 응답 객체인 `ApiResponse`를 사용합니다.

```json
{
  "code": "COURSE_RETRIEVED",
  "message": "강좌 조회 성공",
  "data": {}
}
```

단, 삭제 API는 응답 본문 없이 `204 No Content`를 반환합니다.

---

## 3. Course API 목록

| 기능 | Method | URL | Controller |
|---|---|---|---|
| 강좌 목록 조회 | GET | `/api/v1/courses` | `CourseController` |
| 강좌 상세 조회 | GET | `/api/v1/courses/{courseId}` | `CourseController` |
| 강좌 생성 | POST | `/api/v1/courses` | `CourseController` |
| 강좌 수정 | PUT | `/api/v1/courses/{courseId}` | `CourseController` |
| 강좌 공개 | PATCH | `/api/v1/courses/{courseId}/publish` | `CourseController` |
| 강좌 삭제 | DELETE | `/api/v1/courses/{courseId}` | `CourseController` |
| 강좌 카테고리 조회 | GET | `/api/v1/course-categories` | `CourseCategoryController` |
| 강사별 강좌 조회 | GET | `/api/v1/instructors/{instructorId}/courses` | `InstructorCourseController` |
| 강좌 문제세트 조회 | GET | `/api/v1/courses/{courseId}/problem-sets` | `CourseProblemController` |
| 강의 문제세트 조회 | GET | `/api/v1/lectures/{lectureId}/problem-sets` | `CourseProblemController` |
| 강좌 문제세트 연결 저장 | PUT | `/api/v1/courses/{courseId}/problem-sets` | `CourseProblemController` |

---

## 4. 강좌 목록 조회

### 설명

강좌 목록을 조회합니다.

`courseCategoryId` Query Parameter가 있으면 해당 카테고리에 속한 강좌 목록을 조회합니다.  
없으면 전체 강좌 목록을 조회합니다.

### Endpoint

```http
GET /api/v1/courses
```

### Query Parameters

| 이름 | 타입 | 필수 여부 | 설명 |
|---|---|---:|---|
| `courseCategoryId` | Long | 선택 | 강좌 카테고리 ID |

### Request 예시

```http
GET /api/v1/courses
```

```http
GET /api/v1/courses?courseCategoryId=1
```

### Response 예시

```json
{
  "code": "COURSE_RETRIEVED",
  "message": "강좌 조회 성공",
  "data": [
    {
      "courseId": 1,
      "instructorId": 10,
      "courseCategoryId": 1,
      "courseCategoryName": "Backend",
      "title": "Spring Boot 기초",
      "thumbnailUrl": "https://example.com/course-thumbnail.png",
      "status": "ACTIVE"
    }
  ]
}
```

### 호출 흐름

```text
CourseController.findAllCourses()
-> CourseQueryUseCase.findAllCourses(courseCategoryId)
-> CourseQueryService
-> CourseRepository
```

---

## 5. 강좌 상세 조회

### 설명

강좌 ID를 기준으로 강좌 상세 정보를 조회합니다.

### Endpoint

```http
GET /api/v1/courses/{courseId}
```

### Path Variables

| 이름 | 타입 | 설명 |
|---|---|---|
| `courseId` | Long | 조회할 강좌 ID |

### Response 예시

```json
{
  "code": "COURSE_RETRIEVED",
  "message": "강좌 조회 성공",
  "data": {
    "courseId": 1,
    "instructorId": 10,
    "courseCategoryId": 1,
    "courseCategoryName": "Backend",
    "title": "Spring Boot 기초",
    "description": "Spring Boot 기본 개념을 학습하는 강좌입니다.",
    "thumbnailUrl": "https://example.com/course-thumbnail.png",
    "status": "ACTIVE",
    "createdAt": "2026-05-01T10:00:00",
    "updatedAt": "2026-05-01T10:00:00"
  }
}
```

### 주요 예외

| 상황 | ErrorCode |
|---|---|
| 강좌가 존재하지 않는 경우 | `COURSE_NOT_FOUND` |

### 호출 흐름

```text
CourseController.findCourseById()
-> CourseQueryUseCase.findCourseById(courseId)
-> CourseQueryService
-> CourseRepository
```

---

## 6. 강좌 생성

### 설명

운영자가 새로운 강좌를 생성합니다.

생성된 강좌의 기본 상태는 `DRAFT`입니다.

### Endpoint

```http
POST /api/v1/courses
```

### Request Body

```json
{
  "instructorId": 10,
  "courseCategoryId": 1,
  "title": "Spring Boot 기초",
  "description": "Spring Boot 기본 개념을 학습하는 강좌입니다.",
  "thumbnailUrl": "https://example.com/course-thumbnail.png"
}
```

### Request Fields

| 필드 | 타입 | 필수 여부 | 검증 |
|---|---|---:|---|
| `instructorId` | Long | 필수 | null 불가 |
| `courseCategoryId` | Long | 필수 | null 불가 |
| `title` | String | 필수 | 빈 문자열 불가, 최대 100자 |
| `description` | String | 선택 | 별도 제한 없음 |
| `thumbnailUrl` | String | 선택 | 최대 500자 |

### Response 예시

```json
{
  "code": "COURSE_CREATED",
  "message": "강좌 생성 성공",
  "data": {
    "courseId": 1,
    "instructorId": 10,
    "courseCategoryId": 1,
    "courseCategoryName": "Backend",
    "title": "Spring Boot 기초",
    "description": "Spring Boot 기본 개념을 학습하는 강좌입니다.",
    "thumbnailUrl": "https://example.com/course-thumbnail.png",
    "status": "DRAFT",
    "createdAt": "2026-05-01T10:00:00",
    "updatedAt": "2026-05-01T10:00:00"
  }
}
```

### 비즈니스 규칙

- `instructorId`는 필수입니다.
- `courseCategoryId`는 필수입니다.
- `title`은 필수이며 빈 문자열일 수 없습니다.
- `title`은 100자 이하입니다.
- `thumbnailUrl`은 500자 이하입니다.
- 강좌 생성자는 활성 상태의 운영자여야 합니다.
- 강좌 카테고리는 활성 상태여야 합니다.
- 생성된 강좌의 기본 상태는 `DRAFT`입니다.

### 주요 예외

| 상황 | ErrorCode |
|---|---|
| 강좌 생성자가 운영자가 아닌 경우 | `COURSE_OPERATOR_REQUIRED` |
| 활성 카테고리가 아닌 경우 | `COURSE_CATEGORY_REQUIRED` |

### 호출 흐름

```text
CourseController.createCourse()
-> CourseCreateRequest
-> CreateCourseCommand
-> CourseCommandUseCase.createCourse()
-> CourseCommandService.createCourse()
-> CourseAuthorPolicy.validateOperator()
-> CourseCategoryPolicy.validateActiveCategory()
-> Course.create()
-> CourseRepository.save()
```

---

## 7. 강좌 수정

### 설명

강좌 정보를 수정합니다.

### Endpoint

```http
PUT /api/v1/courses/{courseId}
```

### Path Variables

| 이름 | 타입 | 설명 |
|---|---|---|
| `courseId` | Long | 수정할 강좌 ID |

### Request Body

```json
{
  "courseCategoryId": 1,
  "title": "Spring Boot 심화",
  "description": "Spring Boot 심화 개념을 학습하는 강좌입니다.",
  "thumbnailUrl": "https://example.com/updated-thumbnail.png",
  "status": "INACTIVE"
}
```

### Request Fields

| 필드 | 타입 | 필수 여부 | 검증 |
|---|---|---:|---|
| `courseCategoryId` | Long | 선택 | 값이 있으면 활성 카테고리 검증 |
| `title` | String | 선택 | 최대 100자 |
| `description` | String | 선택 | 별도 제한 없음 |
| `thumbnailUrl` | String | 선택 | 최대 500자 |
| `status` | CourseStatus | 선택 | `ACTIVE`, `DELETED` 직접 변경 제한 |

### Response 예시

```json
{
  "code": "COURSE_UPDATED",
  "message": "강좌 수정 성공",
  "data": {
    "courseId": 1,
    "instructorId": 10,
    "courseCategoryId": 1,
    "courseCategoryName": "Backend",
    "title": "Spring Boot 심화",
    "description": "Spring Boot 심화 개념을 학습하는 강좌입니다.",
    "thumbnailUrl": "https://example.com/updated-thumbnail.png",
    "status": "INACTIVE",
    "createdAt": "2026-05-01T10:00:00",
    "updatedAt": "2026-05-02T10:00:00"
  }
}
```

### 비즈니스 규칙

- 존재하지 않는 강좌는 수정할 수 없습니다.
- 삭제된 강좌는 수정할 수 없습니다.
- 카테고리를 변경하는 경우 활성 카테고리인지 검증합니다.
- `ACTIVE` 상태로 직접 변경할 수 없습니다.
- 강좌 활성화는 강좌 공개 API를 통해서만 수행합니다.
- `DELETED` 상태로 직접 변경할 수 없습니다.
- 강좌 삭제는 강좌 삭제 API를 통해서만 수행합니다.

### 주요 예외

| 상황 | ErrorCode |
|---|---|
| 강좌가 존재하지 않는 경우 | `COURSE_NOT_FOUND` |
| 활성 카테고리가 아닌 경우 | `COURSE_CATEGORY_REQUIRED` |
| `ACTIVE` 상태로 직접 변경하려는 경우 | `COURSE_ACTIVE_STATUS_REQUIRES_PUBLISH` |
| `DELETED` 상태로 직접 변경하려는 경우 | `COURSE_DELETE_STATUS_REQUIRES_DELETE` |

### 호출 흐름

```text
CourseController.updateCourse()
-> CourseUpdateRequest
-> UpdateCourseCommand
-> CourseCommandUseCase.updateCourse()
-> CourseCommandService.updateCourse()
-> CourseCategoryPolicy.validateActiveCategory()
-> Course.update()
-> CourseRepository.save()
```

---

## 8. 강좌 공개

### 설명

`DRAFT` 상태의 강좌를 공개합니다.

공개에 성공하면 강좌 상태는 `ACTIVE`가 됩니다.

### Endpoint

```http
PATCH /api/v1/courses/{courseId}/publish
```

### Path Variables

| 이름 | 타입 | 설명 |
|---|---|---|
| `courseId` | Long | 공개할 강좌 ID |

### Response 예시

```json
{
  "code": "COURSE_PUBLISHED",
  "message": "강좌 공개 성공",
  "data": {
    "courseId": 1,
    "instructorId": 10,
    "courseCategoryId": 1,
    "courseCategoryName": "Backend",
    "title": "Spring Boot 기초",
    "description": "Spring Boot 기본 개념을 학습하는 강좌입니다.",
    "thumbnailUrl": "https://example.com/course-thumbnail.png",
    "status": "ACTIVE",
    "createdAt": "2026-05-01T10:00:00",
    "updatedAt": "2026-05-02T10:00:00"
  }
}
```

### 비즈니스 규칙

- 존재하지 않는 강좌는 공개할 수 없습니다.
- 삭제된 강좌는 공개할 수 없습니다.
- `DRAFT` 상태의 강좌만 공개할 수 있습니다.
- 강좌에 하나 이상의 강의가 존재해야 공개할 수 있습니다.
- 공개 성공 시 강좌 상태는 `ACTIVE`가 됩니다.

### 주요 예외

| 상황 | ErrorCode |
|---|---|
| 강좌가 존재하지 않는 경우 | `COURSE_NOT_FOUND` |
| 강좌 상태가 `DRAFT`가 아닌 경우 | `COURSE_NOT_PUBLISHABLE_STATUS` |
| 강좌에 연결된 강의가 없는 경우 | `COURSE_LECTURE_REQUIRED` |

### 호출 흐름

```text
CourseController.publishCourse()
-> PublishCourseCommand
-> CourseCommandUseCase.publishCourse()
-> CourseCommandService.publishCourse()
-> CourseRepository.findByCourseIdAndDeletedAtIsNull()
-> CoursePublishPolicy.validate()
-> LectureCatalogPort.existsLectureInCourse()
-> Course.publish()
-> CourseRepository.save()
```

---

## 9. 강좌 삭제

### 설명

강좌를 삭제합니다.

현재 구현은 강좌 상태를 `DELETED`로 변경하고 `deletedAt`을 기록하는 Soft Delete 방식입니다.

### Endpoint

```http
DELETE /api/v1/courses/{courseId}
```

### Path Variables

| 이름 | 타입 | 설명 |
|---|---|---|
| `courseId` | Long | 삭제할 강좌 ID |

### Response

```text
204 No Content
```

### 비즈니스 규칙

- 존재하지 않는 강좌는 삭제할 수 없습니다.
- 삭제는 Soft Delete 방식으로 처리합니다.
- 삭제 시 강좌 상태는 `DELETED`가 됩니다.
- 삭제 시 `deletedAt`이 기록됩니다.
- 강좌 삭제 시 연결된 강의도 함께 삭제 처리합니다.

### 주요 예외

| 상황 | ErrorCode |
|---|---|
| 강좌가 존재하지 않는 경우 | `COURSE_NOT_FOUND` |

### 호출 흐름

```text
CourseController.deleteCourse()
-> CourseCommandUseCase.deleteCourse(courseId)
-> CourseCommandService.deleteCourse()
-> CourseRepository.findByCourseIdAndDeletedAtIsNull()
-> Course.delete()
-> CourseRepository.save()
-> LectureManagementPort.deleteLecturesByCourseId()
```

---

## 10. 강좌 카테고리 조회

### 설명

활성 상태의 강좌 카테고리 목록을 조회합니다.

### Endpoint

```http
GET /api/v1/course-categories
```

### Response 예시

```json
{
  "code": "COURSE_RETRIEVED",
  "message": "강좌 조회 성공",
  "data": [
    {
      "courseCategoryId": 1,
      "name": "Backend",
      "displayOrder": 1
    },
    {
      "courseCategoryId": 2,
      "name": "Frontend",
      "displayOrder": 2
    }
  ]
}
```

### 비즈니스 규칙

- 활성 상태의 카테고리만 조회합니다.
- 카테고리는 표시 순서 기준으로 정렬합니다.

### 호출 흐름

```text
CourseCategoryController.findCourseCategories()
-> CourseCategoryQueryUseCase.findCourseCategories()
-> CourseCategoryQueryService
-> CourseCategoryRepository.findActiveCategories()
```

---

## 11. 강사별 강좌 조회

### 설명

강사 ID를 기준으로 해당 강사가 생성한 강좌 목록을 조회합니다.

### Endpoint

```http
GET /api/v1/instructors/{instructorId}/courses
```

### Path Variables

| 이름 | 타입 | 설명 |
|---|---|---|
| `instructorId` | Long | 강사 또는 운영자 사용자 ID |

### Response 예시

```json
{
  "code": "COURSE_RETRIEVED",
  "message": "강좌 조회 성공",
  "data": [
    {
      "courseId": 1,
      "instructorId": 10,
      "courseCategoryId": 1,
      "courseCategoryName": "Backend",
      "title": "Spring Boot 기초",
      "thumbnailUrl": "https://example.com/course-thumbnail.png",
      "status": "ACTIVE"
    }
  ]
}
```

### 호출 흐름

```text
InstructorCourseController.findCoursesByInstructor()
-> CourseQueryUseCase.findCoursesByInstructor(instructorId)
-> CourseQueryService
-> CourseRepository
```

---

## 12. 강좌 문제세트 조회

### 설명

강좌 ID를 기준으로 강좌에 연결된 문제세트 목록을 조회합니다.

### Endpoint

```http
GET /api/v1/courses/{courseId}/problem-sets
```

### Path Variables

| 이름 | 타입 | 설명 |
|---|---|---|
| `courseId` | Long | 조회할 강좌 ID |

### Response 예시

```json
{
  "code": "COURSE_RETRIEVED",
  "message": "강좌 조회 성공",
  "data": [
    {
      "courseProblemSetId": 1,
      "courseId": 1,
      "lectureId": 3,
      "problemSetId": 10,
      "role": "MAIN",
      "displayOrder": 1
    }
  ]
}
```

### 호출 흐름

```text
CourseProblemController.findProblemSetsByCourse()
-> CourseProblemQueryUseCase.findProblemSetsByCourse(courseId)
-> CourseProblemQueryService
-> CourseProblemSetRepository
```

---

## 13. 강의 문제세트 조회

### 설명

강의 ID를 기준으로 해당 강의에 연결된 문제세트 목록을 조회합니다.

### Endpoint

```http
GET /api/v1/lectures/{lectureId}/problem-sets
```

### Path Variables

| 이름 | 타입 | 설명 |
|---|---|---|
| `lectureId` | Long | 조회할 강의 ID |

### Response 예시

```json
{
  "code": "COURSE_RETRIEVED",
  "message": "강좌 조회 성공",
  "data": [
    {
      "courseProblemSetId": 1,
      "courseId": 1,
      "lectureId": 3,
      "problemSetId": 10,
      "role": "MAIN",
      "displayOrder": 1
    }
  ]
}
```

### 호출 흐름

```text
CourseProblemController.findProblemSetsByLecture()
-> CourseProblemQueryUseCase.findProblemSetsByLecture(lectureId)
-> CourseProblemQueryService
-> CourseProblemSetRepository
```

---

## 14. 강좌 문제세트 연결 저장

### 설명

강좌에 연결할 문제세트 목록을 저장합니다.

요청에 포함된 `lectureId`, `problemSetId` 조합을 기준으로 기존 연결이 있으면 갱신하고, 없으면 새로 저장합니다.

### Endpoint

```http
PUT /api/v1/courses/{courseId}/problem-sets
```

### Path Variables

| 이름 | 타입 | 설명 |
|---|---|---|
| `courseId` | Long | 문제세트를 연결할 강좌 ID |

### Request Body

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
      "lectureId": 2,
      "problemSetId": 11,
      "role": "FINAL",
      "displayOrder": 2
    }
  ]
}
```

### Request Fields

| 필드 | 타입 | 필수 여부 | 설명 |
|---|---|---:|---|
| `problemSets` | Array | 필수 | 연결할 문제세트 목록 |
| `problemSets[].lectureId` | Long | 필수 | 강의 ID |
| `problemSets[].problemSetId` | Long | 필수 | 문제세트 ID |
| `problemSets[].role` | CourseProblemSetRole | 필수 | `MAIN`, `FINAL` |
| `problemSets[].displayOrder` | Integer | 필수 | 표시 순서 |

### Response 예시

```json
{
  "code": "COURSE_UPDATED",
  "message": "강좌 수정 성공",
  "data": [
    {
      "courseProblemSetId": 1,
      "courseId": 1,
      "lectureId": 1,
      "problemSetId": 10,
      "role": "MAIN",
      "displayOrder": 1
    },
    {
      "courseProblemSetId": 2,
      "courseId": 1,
      "lectureId": 2,
      "problemSetId": 11,
      "role": "FINAL",
      "displayOrder": 2
    }
  ]
}
```

### 비즈니스 규칙

- 요청한 문제세트 목록은 비어 있을 수 없습니다.
- `lectureId`는 필수입니다.
- `problemSetId`는 필수입니다.
- `role`은 필수입니다.
- `displayOrder`는 필수입니다.
- 요청한 문제세트는 존재해야 합니다.
- 요청한 강의는 해당 강좌에 포함되어 있어야 합니다.
- 기존에 같은 `lectureId`, `problemSetId` 조합이 있으면 갱신합니다.
- 기존 연결이 없으면 새로 저장합니다.

### 주요 예외

| 상황 | ErrorCode |
|---|---|
| 문제세트 목록이 비어 있는 경우 | `COURSE_PROBLEM_SET_REQUIRED` |
| 필수 연결 정보가 누락된 경우 | `COURSE_PROBLEM_SET_REQUIRED` |
| 존재하지 않는 문제세트인 경우 | `COURSE_PROBLEM_SET_NOT_FOUND` |
| 해당 강좌에 존재하지 않는 강의인 경우 | `COURSE_PROBLEM_LECTURE_NOT_FOUND` |

### 호출 흐름

```text
CourseProblemController.configureProblemSets()
-> CourseProblemSetConfigureRequest
-> ConfigureCourseProblemSetsCommand
-> CourseProblemCommandUseCase.configureProblemSets()
-> CourseProblemCommandService.configureProblemSets()
-> CourseProblemPolicy.validate()
-> ProblemCatalogPort.existsProblemSet()
-> LectureCatalogPort.existsLectureInCourse()
-> CourseProblemSetRepository.save()
```

---

## 15. CourseStatus

| 상태 | 설명 |
|---|---|
| `DRAFT` | 작성 중인 강좌 |
| `ACTIVE` | 공개된 강좌 |
| `INACTIVE` | 비활성화된 강좌 |
| `DELETED` | 삭제 처리된 강좌 |

---

## 16. CourseProblemSetRole

| 역할 | 설명 |
|---|---|
| `MAIN` | 강의별 주요 문제세트 |
| `FINAL` | 강좌 최종 문제세트 |

---

## 17. 에러 코드

Course 도메인 에러코드는 `CourseErrorCode`에서 관리합니다.

| ErrorCode | Code | Message |
|---|---|---|
| `COURSE_NOT_FOUND` | `CRS-001` | 존재하지 않는 강좌입니다. |
| `COURSE_LECTURE_REQUIRED` | `CRS-002` | 강좌를 개설하려면 강의가 1개 이상 필요합니다. |
| `COURSE_ACTIVE_STATUS_REQUIRES_PUBLISH` | `CRS-003` | 강좌 활성화는 개설 기능을 통해서만 가능합니다. |
| `COURSE_NOT_PUBLISHABLE_STATUS` | `CRS-004` | 작성 중인 강좌만 개설할 수 있습니다. |
| `COURSE_DELETE_STATUS_REQUIRES_DELETE` | `CRS-005` | 강좌 삭제는 삭제 기능을 통해서만 가능합니다. |
| `COURSE_OPERATOR_REQUIRED` | `CRS-006` | 강좌는 운영자만 생성할 수 있습니다. |
| `COURSE_CATEGORY_REQUIRED` | `CRS-007` | 활성화된 강좌 카테고리를 선택해야 합니다. |
| `COURSE_PROBLEM_SET_REQUIRED` | `CRS-008` | 강좌에 연결할 문제세트가 필요합니다. |
| `COURSE_PROBLEM_STEP_REQUIRED` | `CRS-009` | 강좌 문제 연결 단계 정보가 필요합니다. |
| `COURSE_PROBLEM_SET_NOT_FOUND` | `CRS-010` | 존재하지 않는 문제세트입니다. |
| `COURSE_PROBLEM_NOT_FOUND` | `CRS-011` | 선택한 문제세트에 존재하지 않는 문제입니다. |
| `COURSE_PROBLEM_LECTURE_REQUIRED` | `CRS-012` | MAIN 문제 단계에는 강의가 필요합니다. |
| `COURSE_PROBLEM_LECTURE_NOT_FOUND` | `CRS-013` | 선택한 강좌에 존재하지 않는 강의입니다. |

---

## 18. 구현 위치

| 분류 | 경로 |
|---|---|
| Controller | `src/main/java/com/wanted/codebombalms/course/controller` |
| Request DTO | `src/main/java/com/wanted/codebombalms/course/presentation/api/request` |
| Response DTO | `src/main/java/com/wanted/codebombalms/course/presentation/api/response` |
| Command | `src/main/java/com/wanted/codebombalms/course/application/command` |
| UseCase | `src/main/java/com/wanted/codebombalms/course/application/usecase` |
| Service | `src/main/java/com/wanted/codebombalms/course/application/service` |
| Policy | `src/main/java/com/wanted/codebombalms/course/application/policy` |
| Domain Model | `src/main/java/com/wanted/codebombalms/course/domain/model` |
| Repository Port | `src/main/java/com/wanted/codebombalms/course/domain/repository` |
| JPA Adapter | `src/main/java/com/wanted/codebombalms/course/infrastructure/persistence` |
