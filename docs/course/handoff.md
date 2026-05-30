# Course 도메인 인수인계 문서

## 1. 문서 목적

이 문서는 Course 도메인 작업 현황과 이어서 작업할 때 확인해야 할 내용을 정리한 인수인계 문서입니다.

Course 도메인은 강좌 생성, 조회, 수정, 공개, 삭제와 강좌 카테고리 조회, 강좌-문제세트 연결 기능을 담당합니다.

---

## 2. 현재 구현 상태

현재 Course 도메인에는 다음 기능이 구현되어 있습니다.

| 기능 | 상태 |
|---|---|
| 강좌 생성 | 구현 완료 |
| 강좌 목록 조회 | 구현 완료 |
| 강좌 상세 조회 | 구현 완료 |
| 카테고리별 강좌 목록 조회 | 구현 완료 |
| 강사별 강좌 목록 조회 | 구현 완료 |
| 강좌 수정 | 구현 완료 |
| 강좌 공개 | 구현 완료 |
| 강좌 삭제 | 구현 완료 |
| 강좌 카테고리 조회 | 구현 완료 |
| 강좌 문제세트 조회 | 구현 완료 |
| 강의 문제세트 조회 | 구현 완료 |
| 강좌 문제세트 연결 저장 | 구현 완료 |
| CoursePublishedEvent 발행 | 미연결 |
| Controller 위치 통일 | 미완료 |

---

## 3. 주요 API

| 기능 | Method | URL |
|---|---|---|
| 강좌 목록 조회 | GET | `/api/v1/courses` |
| 강좌 상세 조회 | GET | `/api/v1/courses/{courseId}` |
| 강좌 생성 | POST | `/api/v1/courses` |
| 강좌 수정 | PUT | `/api/v1/courses/{courseId}` |
| 강좌 공개 | PATCH | `/api/v1/courses/{courseId}/publish` |
| 강좌 삭제 | DELETE | `/api/v1/courses/{courseId}` |
| 강좌 카테고리 조회 | GET | `/api/v1/course-categories` |
| 강사별 강좌 조회 | GET | `/api/v1/instructors/{instructorId}/courses` |
| 강좌 문제세트 조회 | GET | `/api/v1/courses/{courseId}/problem-sets` |
| 강의 문제세트 조회 | GET | `/api/v1/lectures/{lectureId}/problem-sets` |
| 강좌 문제세트 연결 저장 | PUT | `/api/v1/courses/{courseId}/problem-sets` |

자세한 내용은 `api-spec.md`를 참고합니다.

---

## 4. 현재 패키지 위치

```text
src/main/java/com/wanted/codebombalms/course/
 ├─ controller/
 ├─ presentation/
 │   └─ api/
 │       ├─ request/
 │       └─ response/
 ├─ application/
 │   ├─ command/
 │   ├─ policy/
 │   ├─ port/
 │   ├─ service/
 │   └─ usecase/
 ├─ domain/
 │   ├─ event/
 │   ├─ exception/
 │   ├─ model/
 │   └─ repository/
 └─ infrastructure/
     ├─ lecture/
     ├─ persistence/
     ├─ problem/
     └─ user/
```

---

## 5. 주요 클래스

## 5.1 Controller

```text
course/controller/CourseController.java
course/controller/CourseCategoryController.java
course/controller/CourseProblemController.java
course/controller/InstructorCourseController.java
```

역할:

- Course 관련 API 요청 처리
- Request DTO 검증
- UseCase 호출
- Response DTO 변환
- ApiResponse 반환

---

## 5.2 Request DTO

```text
course/presentation/api/request/CourseCreateRequest.java
course/presentation/api/request/CourseUpdateRequest.java
course/presentation/api/request/CourseProblemSetConfigureRequest.java
```

역할:

- API 요청 값을 받습니다.
- Bean Validation을 통해 기본 입력값을 검증합니다.
- `CourseProblemSetConfigureRequest`는 내부에서 Command 변환 메서드를 제공합니다.

---

## 5.3 Response DTO

```text
course/presentation/api/response/CourseResponse.java
course/presentation/api/response/CourseDetailResponse.java
course/presentation/api/response/CourseCategoryResponse.java
course/presentation/api/response/CourseProblemSetResponse.java
```

역할:

- Domain Model을 API 응답 형식으로 변환합니다.
- JPA Entity를 직접 반환하지 않습니다.

---

## 5.4 Application

```text
course/application/command
course/application/usecase
course/application/service
course/application/policy
course/application/port
```

역할:

- UseCase 흐름 조립
- 트랜잭션 처리
- 정책 검증
- Port를 통한 외부 도메인 연결
- Domain Model 저장

---

## 5.5 Domain

```text
course/domain/model
course/domain/repository
course/domain/exception
course/domain/event
```

역할:

- Course 도메인 모델 관리
- Repository Port 정의
- Course 도메인 예외 정의
- Course 도메인 이벤트 정의

---

## 5.6 Infrastructure

```text
course/infrastructure/persistence
course/infrastructure/user
course/infrastructure/lecture
course/infrastructure/problem
```

역할:

- JPA Entity 관리
- Spring Data Repository 관리
- Repository Port 구현
- 다른 도메인 Adapter 구현

---

## 6. 핵심 비즈니스 규칙

## 6.1 강좌 생성

- 강좌 생성자는 활성 운영자여야 합니다.
- 강좌 카테고리는 활성 상태여야 합니다.
- 강좌 제목은 필수입니다.
- 강좌 제목은 100자 이하입니다.
- 썸네일 URL은 500자 이하입니다.
- 생성된 강좌의 기본 상태는 `DRAFT`입니다.

관련 클래스:

```text
CourseCreateRequest
CreateCourseCommand
CourseCommandService
CourseAuthorPolicy
CourseCategoryPolicy
Course
```

---

## 6.2 강좌 조회

- 강좌 목록 조회는 `ACTIVE` 상태이며 삭제되지 않은 강좌만 반환합니다.
- 강좌 상세 조회도 `ACTIVE` 상태이며 삭제되지 않은 강좌만 반환합니다.
- 카테고리 ID가 있으면 해당 카테고리의 `ACTIVE` 강좌만 조회합니다.
- 강사별 강좌 조회는 해당 강사의 삭제되지 않은 강좌를 반환합니다.

관련 클래스:

```text
CourseQueryService
CourseRepository
```

---

## 6.3 강좌 수정

- 존재하지 않는 강좌는 수정할 수 없습니다.
- 삭제된 강좌는 수정할 수 없습니다.
- 카테고리를 변경하는 경우 활성 카테고리인지 검증합니다.
- 수정 API로 `ACTIVE` 상태를 직접 지정할 수 없습니다.
- 수정 API로 `DELETED` 상태를 직접 지정할 수 없습니다.

관련 클래스:

```text
CourseUpdateRequest
UpdateCourseCommand
CourseCommandService
CourseCategoryPolicy
Course
```

---

## 6.4 강좌 공개

- 존재하지 않는 강좌는 공개할 수 없습니다.
- 삭제된 강좌는 공개할 수 없습니다.
- `DRAFT` 상태의 강좌만 공개할 수 있습니다.
- 강좌에 하나 이상의 강의가 있어야 공개할 수 있습니다.
- 공개 성공 시 상태는 `ACTIVE`가 됩니다.

관련 클래스:

```text
PublishCourseCommand
CourseCommandService
CoursePublishPolicy
LectureCatalogPort
Course
```

---

## 6.5 강좌 삭제

- 존재하지 않는 강좌는 삭제할 수 없습니다.
- 삭제는 Soft Delete 방식으로 처리합니다.
- 삭제 시 상태는 `DELETED`가 됩니다.
- 삭제 시 `deletedAt`이 기록됩니다.
- 강좌 삭제 시 연결된 강의도 함께 삭제 처리합니다.

관련 클래스:

```text
CourseCommandService
Course
LectureManagementPort
```

---

## 6.6 강좌 문제세트 연결

- 문제세트 연결 목록은 비어 있을 수 없습니다.
- `lectureId`, `problemSetId`, `role`, `displayOrder`는 필수입니다.
- 문제세트는 존재해야 합니다.
- 강의는 해당 강좌에 포함되어 있어야 합니다.
- 현재 구현은 기존 강좌 문제세트 연결을 전체 삭제한 뒤 요청 목록 기준으로 재저장합니다.

관련 클래스:

```text
CourseProblemSetConfigureRequest
ConfigureCourseProblemSetsCommand
CourseProblemCommandService
CourseProblemPolicy
ProblemCatalogPort
LectureCatalogPort
CourseProblemSetRepository
```

---

## 7. 연관 도메인

| 도메인 | 연결 방식 | 설명 |
|---|---|---|
| User | `UserCatalogPort`, `UserAdapter` | 강좌 생성자가 활성 운영자인지 확인 |
| Lecture | `LectureCatalogPort`, `LectureManagementPort` | 강좌 공개 전 강의 존재 여부 확인, 강좌 삭제 시 강의 삭제 |
| Problems | `ProblemCatalogPort`, `ProblemCatalogAdapter` | 문제세트 존재 여부 확인 |
| Enrollment | Course ID 기반 연결 | 수강 신청과 연결 |
| Learning | Course ID 기반 연결 | 학습 진행률과 연결 |

---

## 8. 현재 주의할 점

## 8.1 Controller 위치

현재 Controller는 다음 위치에 있습니다.

```text
course/controller
```

하지만 Request/Response DTO는 다음 위치에 있습니다.

```text
course/presentation/api/request
course/presentation/api/response
```

프로젝트 구조 통일 기준을 강하게 적용한다면 Controller도 다음 위치로 이동하는 것이 좋습니다.

```text
course/presentation/api
```

다만 파일 이동은 package 선언, import, 테스트 코드까지 같이 수정해야 하므로 별도 리팩터링 이슈로 분리하는 것을 권장합니다.

---

## 8.2 CoursePublishedEvent

현재 `CoursePublishedEvent`는 정의되어 있습니다.

```text
course/domain/event/CoursePublishedEvent.java
```

하지만 현재 공개 흐름에서는 이벤트 기록 및 발행 흐름이 연결되어 있지 않습니다.

추후 적용 방향:

```text
Course.publish()
-> CoursePublishedEvent 기록
-> CourseCommandService에서 이벤트 발행
-> EventHandler에서 후속 처리
```

---

## 8.3 CourseProblemSet 저장 방식

현재 `CourseProblemCommandService`는 강좌 문제세트 연결 저장 시 기존 연결을 삭제한 뒤 재저장합니다.

현재 흐름:

```text
courseProblemSetRepository.deleteByCourseId(courseId)
-> 요청 목록 반복 저장
```

따라서 “기존 연결 갱신”이라는 표현을 문서에 사용할 때는 현재 구현과 맞는지 확인해야 합니다.

---

## 9. 다음 작업 추천

## 9.1 문서 정비

- Course API 명세 정리
- Course Clean Architecture 문서 정리
- Course 컨벤션 문서 정리
- Course 인수인계 문서 정리
- 프로젝트 구조 통일 문서와 연결

---

## 9.2 리팩터링 후보

| 우선순위 | 작업 |
|---|---|
| 1 | Controller 위치를 `presentation/api`로 이동 |
| 2 | CoursePublishedEvent 발행 흐름 연결 |
| 3 | CourseProblemSet 저장 방식을 갱신/추가/삭제 분리 방식으로 개선 |
| 4 | Course 관련 테스트 코드 보강 |
| 5 | Swagger 설명 보강 |

---

## 10. 테스트 시 확인할 기능

Course 도메인 테스트 또는 수동 검증 시 다음 흐름을 확인합니다.

```text
1. 강좌 생성
2. 강좌 목록 조회
3. 강좌 상세 조회
4. 강좌 수정
5. 강의 등록
6. 강좌 공개
7. 강좌 문제세트 연결
8. 강좌 문제세트 조회
9. 강좌 삭제
```

---

## 11. PR 작성 시 참고 문구

PR 제목 예시:

```text
[Docs] Course 도메인 문서 추가
```

작업 내용 예시:

```md
- Course API 명세 문서 추가
- Course Clean Architecture 구조 문서 추가
- Course 도메인 개발 컨벤션 문서 추가
- Course 도메인 인수인계 문서 추가
- Course 프로젝트 구조 컨벤션 문서 추가
```

---

## 12. 관련 문서

| 문서 | 설명 |
|---|---|
| `docs/Course/api-spec.md` | Course API 명세 |
| `docs/Course/clean_architecture_plan.md` | Course 계층 구조 및 설계 방향 |
| `docs/Course/convention.md` | Course 개발 컨벤션 |
| `docs/Course/course_project_convention.md` | Course 패키지 구조 컨벤션 |
| `docs/CONVENTION.md` | 프로젝트 공통 컨벤션 |
