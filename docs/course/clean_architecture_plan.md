# Course Clean Architecture Plan

## 1. 문서 목적

이 문서는 Code-Bomba-LMS 프로젝트의 Course 도메인이 Clean Architecture 구조를 어떻게 따르고 있는지 정리하는 문서입니다.

Course 도메인은 강좌 생성, 조회, 수정, 공개, 삭제와 강좌 카테고리 조회, 강좌-문제세트 연결 기능을 담당합니다.

본 문서는 다음 내용을 정리합니다.

- Course 도메인의 계층 구조
- 계층별 책임
- 의존성 방향
- 현재 구조와 개선 방향
- 추후 리팩터링 기준

---

## 2. Course 도메인 현재 구조

현재 Course 도메인은 다음 구조로 구성되어 있습니다.

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

현재 Controller는 `course/controller`에 있고, Request/Response DTO는 `course/presentation/api` 하위에 있습니다.

향후 구조를 더 통일한다면 Controller도 `presentation/api` 하위로 이동하는 것을 고려할 수 있습니다.

---

## 3. 목표 구조

Course 도메인의 목표 구조는 다음과 같습니다.

```text
course/
 ├─ presentation/
 │   └─ api/
 │       ├─ CourseController.java
 │       ├─ CourseCategoryController.java
 │       ├─ CourseProblemController.java
 │       ├─ InstructorCourseController.java
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
     ├─ persistence/
     ├─ user/
     ├─ lecture/
     └─ problem/
```

---

## 4. 계층별 책임

## 4.1 Presentation 계층

현재 위치:

```text
course/controller
course/presentation/api/request
course/presentation/api/response
```

목표 위치:

```text
course/presentation/api
```

역할:

- HTTP 요청을 받습니다.
- Request DTO를 검증합니다.
- Request DTO를 Application Command로 변환합니다.
- UseCase를 호출합니다.
- Domain Model을 Response DTO로 변환합니다.
- 공통 응답 형식인 `ApiResponse`로 반환합니다.

주요 클래스:

| 클래스 | 역할 |
|---|---|
| `CourseController` | 강좌 생성, 조회, 수정, 공개, 삭제 API |
| `CourseCategoryController` | 강좌 카테고리 조회 API |
| `CourseProblemController` | 강좌/강의별 문제세트 조회 및 연결 API |
| `InstructorCourseController` | 강사별 강좌 조회 API |
| `CourseCreateRequest` | 강좌 생성 요청 DTO |
| `CourseUpdateRequest` | 강좌 수정 요청 DTO |
| `CourseProblemSetConfigureRequest` | 강좌 문제세트 연결 요청 DTO |
| `CourseResponse` | 강좌 목록 응답 DTO |
| `CourseDetailResponse` | 강좌 상세 응답 DTO |
| `CourseCategoryResponse` | 강좌 카테고리 응답 DTO |
| `CourseProblemSetResponse` | 강좌 문제세트 응답 DTO |

Presentation 계층에서 하지 않아야 할 일:

- Repository 직접 호출
- JPA Entity 직접 사용
- 복잡한 비즈니스 규칙 판단
- 다른 도메인의 구현체 직접 호출

---

## 4.2 Application 계층

현재 위치:

```text
course/application
```

역할:

- 하나의 UseCase 흐름을 조립합니다.
- 트랜잭션 경계를 가집니다.
- Domain Model을 조회하고 도메인 행위를 호출합니다.
- 여러 도메인 조회가 필요한 검증은 Policy로 분리합니다.
- 외부 도메인 접근은 Port를 통해 수행합니다.

주요 구성:

```text
application/
 ├─ command/
 ├─ policy/
 ├─ port/
 ├─ service/
 └─ usecase/
```

주요 클래스:

| 클래스 | 역할 |
|---|---|
| `CreateCourseCommand` | 강좌 생성 명령 |
| `UpdateCourseCommand` | 강좌 수정 명령 |
| `PublishCourseCommand` | 강좌 공개 명령 |
| `ConfigureCourseProblemSetsCommand` | 강좌 문제세트 연결 명령 |
| `CourseCommandUseCase` | 강좌 명령 UseCase 인터페이스 |
| `CourseQueryUseCase` | 강좌 조회 UseCase 인터페이스 |
| `CourseCategoryQueryUseCase` | 강좌 카테고리 조회 UseCase 인터페이스 |
| `CourseProblemCommandUseCase` | 강좌 문제세트 명령 UseCase 인터페이스 |
| `CourseProblemQueryUseCase` | 강좌 문제세트 조회 UseCase 인터페이스 |
| `CourseCommandService` | 강좌 생성, 수정, 공개, 삭제 처리 |
| `CourseQueryService` | 강좌 목록, 상세, 강사별 강좌 조회 |
| `CourseCategoryQueryService` | 활성 강좌 카테고리 조회 |
| `CourseProblemCommandService` | 강좌 문제세트 연결 저장 |
| `CourseProblemQueryService` | 강좌/강의별 문제세트 조회 |

Application 계층에서 하지 않아야 할 일:

- Controller 의존
- Request/Response DTO 의존
- JPA Entity 의존
- Spring Data Repository 직접 의존
- 화면 응답 형식 판단

---

## 4.3 Domain 계층

현재 위치:

```text
course/domain
```

역할:

- Course 도메인의 핵심 모델을 관리합니다.
- 도메인 상태와 행위를 표현합니다.
- Repository Port를 정의합니다.
- 도메인 전용 예외 코드를 관리합니다.
- 도메인 이벤트를 정의합니다.

주요 구성:

```text
domain/
 ├─ event/
 ├─ exception/
 ├─ model/
 └─ repository/
```

주요 클래스:

| 클래스 | 역할 |
|---|---|
| `Course` | 강좌 도메인 모델 |
| `CourseStatus` | 강좌 상태 |
| `CourseCategory` | 강좌 카테고리 도메인 모델 |
| `CourseCategoryStatus` | 강좌 카테고리 상태 |
| `CourseProblemSet` | 강좌-강의-문제세트 연결 모델 |
| `CourseProblemSetRole` | 문제세트 역할 |
| `CourseRepository` | 강좌 저장소 Port |
| `CourseCategoryRepository` | 강좌 카테고리 저장소 Port |
| `CourseProblemSetRepository` | 강좌 문제세트 저장소 Port |
| `CourseErrorCode` | Course 도메인 에러 코드 |
| `CoursePublishedEvent` | 강좌 공개 이벤트 |

Domain 계층에서 하지 않아야 할 일:

- Controller 의존
- Request/Response DTO 의존
- JPA Entity 의존
- Spring Data JPA 의존
- HTTP 상태 코드 판단
- Swagger 어노테이션 사용

---

## 4.4 Infrastructure 계층

현재 위치:

```text
course/infrastructure
```

역할:

- JPA Entity를 관리합니다.
- Spring Data Repository를 관리합니다.
- Domain Repository Port를 구현합니다.
- Domain Model과 JPA Entity를 변환합니다.
- 다른 도메인 접근을 위한 Adapter를 구현합니다.

주요 구성:

```text
infrastructure/
 ├─ persistence/
 ├─ user/
 ├─ lecture/
 └─ problem/
```

주요 클래스:

| 클래스 | 역할 |
|---|---|
| `CourseJpaEntity` | Course 저장용 JPA Entity |
| `CourseCategoryJpaEntity` | CourseCategory 저장용 JPA Entity |
| `CourseProblemSetJpaEntity` | CourseProblemSet 저장용 JPA Entity |
| `SpringDataCourseRepository` | Course Spring Data Repository |
| `SpringDataCourseCategoryRepository` | CourseCategory Spring Data Repository |
| `SpringDataCourseProblemSetRepository` | CourseProblemSet Spring Data Repository |
| `CourseRepositoryAdapter` | `CourseRepository` 구현체 |
| `CourseCategoryRepositoryAdapter` | `CourseCategoryRepository` 구현체 |
| `CourseProblemSetRepositoryAdapter` | `CourseProblemSetRepository` 구현체 |
| `UserAdapter` | User 도메인 연동 Adapter |
| `LectureCatalogAdapter` | Lecture 조회 Adapter |
| `LectureManagementAdapter` | Lecture 관리 Adapter |
| `ProblemCatalogAdapter` | Problems 도메인 연동 Adapter |

Infrastructure 계층에서 하지 않아야 할 일:

- 비즈니스 규칙을 숨겨서 처리하지 않습니다.
- Controller로 JPA Entity를 직접 반환하지 않습니다.
- Application Service의 UseCase 흐름을 대신 처리하지 않습니다.

---

## 5. 의존성 방향

Course 도메인의 의존성 방향은 다음을 기준으로 합니다.

```text
presentation
    ↓
application
    ↓
domain
    ↑
infrastructure
```

구체적인 의존 흐름은 다음과 같습니다.

```text
Controller
-> UseCase
-> Application Service
-> Policy
-> Domain Model / Repository Port
-> Infrastructure Adapter
-> Spring Data Repository
-> JPA Entity
```

다른 도메인과 연결할 때는 다음 흐름을 사용합니다.

```text
Course Application
-> Port
-> Infrastructure Adapter
-> Other Domain Repository or Service
```

---

## 6. 주요 UseCase 흐름

## 6.1 강좌 생성

```text
CourseController
-> CourseCreateRequest
-> CreateCourseCommand
-> CourseCommandUseCase
-> CourseCommandService
-> CourseAuthorPolicy
-> CourseCategoryPolicy
-> Course.create()
-> CourseRepository
-> CourseRepositoryAdapter
-> SpringDataCourseRepository
```

## 6.2 강좌 수정

```text
CourseController
-> CourseUpdateRequest
-> UpdateCourseCommand
-> CourseCommandUseCase
-> CourseCommandService
-> CourseRepository.findByCourseIdAndDeletedAtIsNull()
-> CourseCategoryPolicy
-> Course.update()
-> CourseRepository.save()
```

## 6.3 강좌 공개

```text
CourseController
-> PublishCourseCommand
-> CourseCommandUseCase
-> CourseCommandService
-> CourseRepository.findByCourseIdAndDeletedAtIsNull()
-> CoursePublishPolicy
-> LectureCatalogPort
-> Course.publish()
-> CourseRepository.save()
```

## 6.4 강좌 삭제

```text
CourseController
-> CourseCommandUseCase
-> CourseCommandService
-> CourseRepository.findByCourseIdAndDeletedAtIsNull()
-> Course.delete()
-> CourseRepository.save()
-> LectureManagementPort
```

## 6.5 강좌 문제세트 연결

```text
CourseProblemController
-> CourseProblemSetConfigureRequest
-> ConfigureCourseProblemSetsCommand
-> CourseProblemCommandUseCase
-> CourseProblemCommandService
-> CourseRepository
-> CourseProblemPolicy
-> ProblemCatalogPort
-> LectureCatalogPort
-> CourseProblemSetRepository
```

---

## 7. 현재 구조에서 확인된 특징

현재 Course 도메인은 Clean Architecture 흐름을 대부분 따르고 있습니다.

좋은 점:

- Application Service가 UseCase 인터페이스를 구현합니다.
- Repository는 Domain 계층에 Port로 정의되어 있습니다.
- Infrastructure 계층에서 Repository Port를 구현합니다.
- JPA Entity와 Domain Model이 분리되어 있습니다.
- 다른 도메인 연결을 Port와 Adapter로 처리합니다.
- 강좌 공개, 운영자 검증, 카테고리 검증, 문제세트 검증이 Policy로 분리되어 있습니다.

개선 가능 지점:

- Controller가 `course/controller`에 있고, DTO는 `course/presentation/api`에 있어 Presentation 구조가 완전히 통일되어 있지 않습니다.
- `CoursePublishedEvent`는 정의되어 있지만 현재 공개 흐름에서 이벤트 발행까지는 연결되어 있지 않습니다.
- `CourseProblemCommandService`는 기존 연결을 갱신하기보다 `deleteByCourseId()` 후 재저장하는 방식으로 동작합니다. 문서와 구현 설명을 일치시켜야 합니다.

---

## 8. 리팩터링 방향

우선순위는 다음과 같습니다.

### 1순위: 문서 기준 확정

- Course 도메인의 현재 구조를 문서화합니다.
- 프로젝트 전체 구조 통일 기준과 비교합니다.
- 현재 구조와 목표 구조를 구분해서 기록합니다.

### 2순위: Controller 위치 통일

현재:

```text
course/controller
```

목표:

```text
course/presentation/api
```

이동 대상:

```text
CourseController.java
CourseCategoryController.java
CourseProblemController.java
InstructorCourseController.java
CourseResponseCode.java
CourseResponseMessage.java
```

### 3순위: Domain Event 흐름 검토

현재:

```text
CoursePublishedEvent 정의만 존재
```

향후:

```text
Course.publish()
-> CoursePublishedEvent 기록
-> Application Service에서 이벤트 발행
```

### 4순위: CourseProblemSet 저장 방식 검토

현재:

```text
기존 courseId 연결 전체 삭제
-> 요청 목록 재저장
```

향후 검토:

```text
기존 연결 갱신
신규 연결 추가
요청에서 빠진 연결 삭제
```

---

## 9. 정리

Course 도메인은 현재 Clean Architecture 구조를 대부분 따르고 있습니다.

다만 Controller 위치와 Domain Event 발행 흐름은 추후 리팩터링 대상으로 남아 있습니다.

현재 문서 작업에서는 실제 코드 이동보다, 현재 구조를 정확히 정리하고 향후 통일 기준을 제안하는 것을 우선합니다.
