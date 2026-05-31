# Course 프로젝트 구조 컨벤션

## 1. 문서 목적

이 문서는 Code-Bomba-LMS 프로젝트의 Course 도메인 패키지 구조와 파일 위치 기준을 정리한 문서입니다.

ChatBot 도메인에 별도 서버 구조 컨벤션 문서가 있는 것처럼, Course 도메인도 Spring Backend 내부에서 어떤 구조로 관리되는지 정리합니다.

---

## 2. 개요

| 항목 | 내용 |
|---|---|
| 도메인 | Course |
| 담당 기능 | 강좌, 강좌 카테고리, 강좌-문제세트 연결 |
| 주요 계층 | Controller, Presentation DTO, Application, Domain, Infrastructure |
| 주요 API Base URL | `/api/v1/courses`, `/api/v1/course-categories`, `/api/v1/instructors`, `/api/v1` |
| 저장 방식 | JPA |
| 응답 형식 | `ApiResponse` |
| 주요 연관 도메인 | User, Lecture, Problems, Enrollment, Learning |

---

## 3. 현재 디렉토리 구조

```text
src/main/java/com/wanted/codebombalms/course/
 ├── controller/
 │   ├── CourseController.java
 │   ├── CourseCategoryController.java
 │   ├── CourseProblemController.java
 │   ├── CourseResponseCode.java
 │   ├── CourseResponseMessage.java
 │   └── InstructorCourseController.java
 │
 ├── presentation/
 │   └── api/
 │       ├── request/
 │       │   ├── CourseCreateRequest.java
 │       │   ├── CourseUpdateRequest.java
 │       │   └── CourseProblemSetConfigureRequest.java
 │       └── response/
 │           ├── CourseResponse.java
 │           ├── CourseDetailResponse.java
 │           ├── CourseCategoryResponse.java
 │           └── CourseProblemSetResponse.java
 │
 ├── application/
 │   ├── command/
 │   │   ├── CreateCourseCommand.java
 │   │   ├── UpdateCourseCommand.java
 │   │   ├── PublishCourseCommand.java
 │   │   └── ConfigureCourseProblemSetsCommand.java
 │   ├── policy/
 │   │   ├── CourseAuthorPolicy.java
 │   │   ├── CourseCategoryPolicy.java
 │   │   ├── CoursePublishPolicy.java
 │   │   └── CourseProblemPolicy.java
 │   ├── port/
 │   │   ├── UserCatalogPort.java
 │   │   ├── LectureCatalogPort.java
 │   │   ├── LectureManagementPort.java
 │   │   └── ProblemCatalogPort.java
 │   ├── service/
 │   │   ├── CourseCommandService.java
 │   │   ├── CourseQueryService.java
 │   │   ├── CourseCategoryQueryService.java
 │   │   ├── CourseProblemCommandService.java
 │   │   └── CourseProblemQueryService.java
 │   └── usecase/
 │       ├── CourseCommandUseCase.java
 │       ├── CourseQueryUseCase.java
 │       ├── CourseCategoryQueryUseCase.java
 │       ├── CourseProblemCommandUseCase.java
 │       └── CourseProblemQueryUseCase.java
 │
 ├── domain/
 │   ├── event/
 │   │   └── CoursePublishedEvent.java
 │   ├── exception/
 │   │   └── CourseErrorCode.java
 │   ├── model/
 │   │   ├── Course.java
 │   │   ├── CourseStatus.java
 │   │   ├── CourseCategory.java
 │   │   ├── CourseCategoryStatus.java
 │   │   ├── CourseProblemSet.java
 │   │   └── CourseProblemSetRole.java
 │   └── repository/
 │       ├── CourseRepository.java
 │       ├── CourseCategoryRepository.java
 │       └── CourseProblemSetRepository.java
 │
 └── infrastructure/
     ├── persistence/
     │   ├── CourseJpaEntity.java
     │   ├── CourseCategoryJpaEntity.java
     │   ├── CourseProblemSetJpaEntity.java
     │   ├── CourseRepositoryAdapter.java
     │   ├── CourseCategoryRepositoryAdapter.java
     │   ├── CourseProblemSetRepositoryAdapter.java
     │   ├── SpringDataCourseRepository.java
     │   ├── SpringDataCourseCategoryRepository.java
     │   └── SpringDataCourseProblemSetRepository.java
     ├── user/
     │   └── UserAdapter.java
     ├── lecture/
     │   ├── LectureCatalogAdapter.java
     │   └── LectureManagementAdapter.java
     └── problem/
         └── ProblemCatalogAdapter.java
```

---

## 4. 권장 목표 구조

프로젝트 전체 구조를 통일한다면 Course 도메인은 아래 구조를 목표로 합니다.

```text
course/
 ├── presentation/
 │   └── api/
 │       ├── CourseController.java
 │       ├── CourseCategoryController.java
 │       ├── CourseProblemController.java
 │       ├── InstructorCourseController.java
 │       ├── CourseResponseCode.java
 │       ├── CourseResponseMessage.java
 │       ├── request/
 │       └── response/
 ├── application/
 │   ├── command/
 │   ├── policy/
 │   ├── port/
 │   ├── service/
 │   └── usecase/
 ├── domain/
 │   ├── event/
 │   ├── exception/
 │   ├── model/
 │   └── repository/
 └── infrastructure/
     ├── persistence/
     ├── user/
     ├── lecture/
     └── problem/
```

현재 코드에서는 Controller만 `course/controller`에 있으므로, 실제 이동은 별도 리팩터링 이슈로 처리합니다.

---

## 5. 패키지별 책임

## 5.1 `controller`

현재 Controller 위치입니다.

```text
course/controller
```

포함 파일:

```text
CourseController.java
CourseCategoryController.java
CourseProblemController.java
InstructorCourseController.java
CourseResponseCode.java
CourseResponseMessage.java
```

책임:

- API 요청 처리
- UseCase 호출
- Response DTO 변환
- ApiResponse 반환

향후 권장 위치:

```text
course/presentation/api
```

---

## 5.2 `presentation/api/request`

Request DTO 위치입니다.

```text
course/presentation/api/request
```

책임:

- 클라이언트 요청 값을 받습니다.
- Bean Validation으로 입력값을 검증합니다.
- 필요 시 Command 변환 메서드를 제공합니다.

포함 파일:

```text
CourseCreateRequest.java
CourseUpdateRequest.java
CourseProblemSetConfigureRequest.java
```

---

## 5.3 `presentation/api/response`

Response DTO 위치입니다.

```text
course/presentation/api/response
```

책임:

- Domain Model을 API 응답 형식으로 변환합니다.
- Controller 응답에서 사용됩니다.
- JPA Entity를 직접 반환하지 않습니다.

포함 파일:

```text
CourseResponse.java
CourseDetailResponse.java
CourseCategoryResponse.java
CourseProblemSetResponse.java
```

---

## 5.4 `application/command`

Command 객체 위치입니다.

```text
course/application/command
```

책임:

- Controller에서 전달받은 요청 값을 Application 계층으로 전달합니다.
- Request DTO와 Application Service 사이의 의존을 분리합니다.

파일명 규칙:

```text
기능명 + Command
```

예시:

```text
CreateCourseCommand
UpdateCourseCommand
PublishCourseCommand
```

---

## 5.5 `application/usecase`

UseCase 인터페이스 위치입니다.

```text
course/application/usecase
```

책임:

- Controller가 의존하는 Application 진입점을 정의합니다.
- Command 기능과 Query 기능을 분리합니다.

파일명 규칙:

```text
도메인명 + CommandUseCase
도메인명 + QueryUseCase
```

예시:

```text
CourseCommandUseCase
CourseQueryUseCase
CourseProblemCommandUseCase
CourseProblemQueryUseCase
```

---

## 5.6 `application/service`

Application Service 구현체 위치입니다.

```text
course/application/service
```

책임:

- UseCase 인터페이스를 구현합니다.
- 트랜잭션 경계를 관리합니다.
- Repository Port를 통해 Domain Model을 조회합니다.
- Policy를 호출합니다.
- Domain Model의 행위를 호출합니다.

파일명 규칙:

```text
도메인명 + CommandService
도메인명 + QueryService
```

예시:

```text
CourseCommandService
CourseQueryService
```

---

## 5.7 `application/policy`

Policy 위치입니다.

```text
course/application/policy
```

책임:

- 여러 도메인 조회가 필요한 검증을 담당합니다.
- Application Service의 조건문을 줄입니다.
- 외부 도메인 접근이 필요한 검증은 Port를 통해 처리합니다.

파일명 규칙:

```text
도메인명 + 목적 + Policy
```

예시:

```text
CourseAuthorPolicy
CourseCategoryPolicy
CoursePublishPolicy
CourseProblemPolicy
```

---

## 5.8 `application/port`

Port 위치입니다.

```text
course/application/port
```

책임:

- 외부 도메인과 연결하기 위한 인터페이스를 정의합니다.
- Application 계층이 외부 구현체에 직접 의존하지 않도록 합니다.

파일명 규칙:

```text
연결대상 + 목적 + Port
```

예시:

```text
UserCatalogPort
LectureCatalogPort
LectureManagementPort
ProblemCatalogPort
```

---

## 5.9 `domain/model`

Domain Model 위치입니다.

```text
course/domain/model
```

책임:

- Course 도메인의 상태와 행위를 표현합니다.
- JPA Entity와 분리합니다.
- DB 저장보다 비즈니스 의미를 우선합니다.

포함 모델:

```text
Course
CourseStatus
CourseCategory
CourseCategoryStatus
CourseProblemSet
CourseProblemSetRole
```

---

## 5.10 `domain/repository`

Repository Port 위치입니다.

```text
course/domain/repository
```

책임:

- Domain Model 저장과 조회에 필요한 인터페이스를 정의합니다.
- Infrastructure 구현체와 Domain 계층을 분리합니다.

포함 파일:

```text
CourseRepository
CourseCategoryRepository
CourseProblemSetRepository
```

---

## 5.11 `domain/exception`

Course 도메인 예외 코드 위치입니다.

```text
course/domain/exception
```

책임:

- Course 도메인에서 사용하는 ErrorCode를 관리합니다.
- 에러 코드 prefix는 `CRS`를 사용합니다.

포함 파일:

```text
CourseErrorCode
```

---

## 5.12 `domain/event`

Course 도메인 이벤트 위치입니다.

```text
course/domain/event
```

책임:

- Course 도메인에서 발생한 중요한 비즈니스 사실을 표현합니다.

포함 파일:

```text
CoursePublishedEvent
```

현재는 이벤트 클래스만 정의되어 있고, 실제 발행 흐름은 연결되어 있지 않습니다.

---

## 5.13 `infrastructure/persistence`

JPA 저장 관련 클래스 위치입니다.

```text
course/infrastructure/persistence
```

책임:

- JPA Entity 관리
- Spring Data Repository 관리
- Repository Port 구현
- Domain Model과 JPA Entity 변환

파일명 규칙:

```text
도메인명 + JpaEntity
SpringData + 도메인명 + Repository
도메인명 + RepositoryAdapter
```

---

## 5.14 `infrastructure/user`

User 도메인 Adapter 위치입니다.

```text
course/infrastructure/user
```

책임:

- Course 도메인에서 User 도메인 정보를 조회합니다.
- 강좌 생성자가 활성 운영자인지 확인할 때 사용합니다.

포함 파일:

```text
UserAdapter
```

---

## 5.15 `infrastructure/lecture`

Lecture 도메인 Adapter 위치입니다.

```text
course/infrastructure/lecture
```

책임:

- 강좌 공개 전 강의 존재 여부를 확인합니다.
- 강좌 삭제 시 연결된 강의를 삭제 처리합니다.

포함 파일:

```text
LectureCatalogAdapter
LectureManagementAdapter
```

---

## 5.16 `infrastructure/problem`

Problems 도메인 Adapter 위치입니다.

```text
course/infrastructure/problem
```

책임:

- 강좌에 연결할 문제세트가 존재하는지 확인합니다.

포함 파일:

```text
ProblemCatalogAdapter
```

---

## 6. 네이밍 규칙

## 6.1 Controller

```text
기능대상 + Controller
```

예시:

```text
CourseController
CourseCategoryController
CourseProblemController
InstructorCourseController
```

---

## 6.2 Request DTO

```text
기능대상 + 동작 + Request
```

예시:

```text
CourseCreateRequest
CourseUpdateRequest
CourseProblemSetConfigureRequest
```

---

## 6.3 Response DTO

```text
기능대상 + Response
기능대상 + DetailResponse
```

예시:

```text
CourseResponse
CourseDetailResponse
CourseCategoryResponse
CourseProblemSetResponse
```

---

## 6.4 Command

```text
동작 + 대상 + Command
```

예시:

```text
CreateCourseCommand
UpdateCourseCommand
PublishCourseCommand
ConfigureCourseProblemSetsCommand
```

---

## 6.5 UseCase

```text
대상 + CommandUseCase
대상 + QueryUseCase
```

예시:

```text
CourseCommandUseCase
CourseQueryUseCase
CourseProblemCommandUseCase
CourseProblemQueryUseCase
```

---

## 6.6 Service

```text
대상 + CommandService
대상 + QueryService
```

예시:

```text
CourseCommandService
CourseQueryService
CourseProblemCommandService
CourseProblemQueryService
```

---

## 6.7 Policy

```text
대상 + 검증목적 + Policy
```

예시:

```text
CourseAuthorPolicy
CourseCategoryPolicy
CoursePublishPolicy
CourseProblemPolicy
```

---

## 6.8 Repository

Domain Repository Port:

```text
대상 + Repository
```

Spring Data Repository:

```text
SpringData + 대상 + Repository
```

Adapter:

```text
대상 + RepositoryAdapter
```

---

## 7. API 경로 규칙

Course 도메인 API는 `/api/v1` 하위에 작성합니다.

| 대상 | URL 기준 |
|---|---|
| 강좌 | `/api/v1/courses` |
| 강좌 카테고리 | `/api/v1/course-categories` |
| 강사별 강좌 | `/api/v1/instructors/{instructorId}/courses` |
| 강좌 문제세트 | `/api/v1/courses/{courseId}/problem-sets` |
| 강의 문제세트 | `/api/v1/lectures/{lectureId}/problem-sets` |

---

## 8. 의존성 규칙

허용되는 의존 방향:

```text
controller
-> application/usecase
-> application/service
-> domain/repository
-> infrastructure/persistence
```

외부 도메인 연결:

```text
application/policy
-> application/port
-> infrastructure/adapter
-> 외부 도메인
```

금지되는 의존:

```text
controller -> infrastructure
application -> presentation
domain -> application
domain -> infrastructure
domain -> Spring Data JPA
domain -> JPA Entity
```

---

## 9. 구조 변경 시 체크리스트

Course 도메인 구조를 변경할 때 다음을 확인합니다.

- [ ] package 선언을 수정했습니다.
- [ ] import 경로를 수정했습니다.
- [ ] 테스트 코드 import를 수정했습니다.
- [ ] Swagger 또는 API 문서 경로를 확인했습니다.
- [ ] `docs/Course` 문서를 함께 수정했습니다.
- [ ] 다른 도메인의 참조 경로를 확인했습니다.
- [ ] 애플리케이션 실행 또는 테스트를 확인했습니다.

---

## 10. 정리

Course 도메인은 현재 Clean Architecture 구조를 대부분 따르고 있습니다.

다만 Controller 위치는 `course/controller`에 남아 있어, 프로젝트 전체 구조를 더 엄격하게 통일하려면 `course/presentation/api`로 이동하는 리팩터링을 고려할 수 있습니다.

문서 작업에서는 현재 구조를 기준으로 정리하고, 실제 이동은 별도 이슈에서 처리합니다.
