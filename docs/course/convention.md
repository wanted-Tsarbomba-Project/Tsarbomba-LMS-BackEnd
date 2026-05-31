# Course 도메인 개발 컨벤션

## 1. 문서 목적

이 문서는 Code-Bomba-LMS 프로젝트의 Course 도메인 개발 시 지켜야 할 규칙을 정리한 문서입니다.

Course 도메인은 강좌 생성, 조회, 수정, 공개, 삭제와 강좌 카테고리 조회, 강좌-문제세트 연결을 담당합니다.

---

## 2. 기본 원칙

Course 도메인 개발 시 다음 원칙을 따릅니다.

- 계층별 책임을 분리합니다.
- Controller에서 비즈니스 규칙을 직접 처리하지 않습니다.
- Application Service는 UseCase 흐름을 조립합니다.
- Domain Model은 도메인 상태와 행위를 표현합니다.
- JPA Entity는 저장 모델로만 사용합니다.
- 다른 도메인 접근은 Port와 Adapter를 통해 처리합니다.
- API 응답은 공통 응답 형식인 `ApiResponse`를 사용합니다.

---

## 3. 패키지 작성 규칙

현재 Course 도메인은 다음 패키지 구조를 사용합니다.

```text
course/
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

향후 구조 통일 시 Controller는 `course/presentation/api`로 이동하는 것을 권장합니다.

---

## 4. Controller 작성 규칙

Controller는 다음 역할만 담당합니다.

- HTTP 요청 수신
- Request DTO 검증
- Request DTO를 Command로 변환
- UseCase 호출
- Response DTO 변환
- `ApiResponse` 반환

Controller에서 하지 말아야 할 일:

- Repository 직접 호출
- JPA Entity 직접 사용
- 비즈니스 규칙 판단
- 다른 도메인 구현체 직접 호출
- 복잡한 조건문으로 도메인 규칙 처리

예시 흐름:

```text
CourseController
-> CourseCreateRequest
-> CreateCourseCommand
-> CourseCommandUseCase
```

---

## 5. Request DTO 작성 규칙

Request DTO는 다음 위치에 둡니다.

```text
course/presentation/api/request
```

현재 Request DTO:

```text
CourseCreateRequest
CourseUpdateRequest
CourseProblemSetConfigureRequest
```

작성 규칙:

- Java `record` 사용을 기본으로 합니다.
- 입력값 검증은 Bean Validation을 사용합니다.
- Request DTO는 Application 계층으로 직접 넘기지 않고 Command로 변환합니다.
- Request DTO는 Presentation 계층 밖에서 사용하지 않습니다.

예시:

```java
public record CourseCreateRequest(
        @NotNull Long instructorId,
        @NotNull Long courseCategoryId,
        @NotBlank @Size(max = 100) String title,
        String description,
        @Size(max = 500) String thumbnailUrl
) {
}
```

---

## 6. Response DTO 작성 규칙

Response DTO는 다음 위치에 둡니다.

```text
course/presentation/api/response
```

현재 Response DTO:

```text
CourseResponse
CourseDetailResponse
CourseCategoryResponse
CourseProblemSetResponse
```

작성 규칙:

- Domain Model을 직접 반환하지 않습니다.
- Response DTO에는 `from()` 정적 팩토리 메서드를 둡니다.
- Controller에서 Domain Model을 Response DTO로 변환합니다.
- JPA Entity를 Response DTO로 직접 변환하지 않습니다.

예시 흐름:

```text
Domain Model
-> CourseDetailResponse.from(course)
-> ApiResponse
```

---

## 7. Command 작성 규칙

Command는 다음 위치에 둡니다.

```text
course/application/command
```

현재 Command:

```text
CreateCourseCommand
UpdateCourseCommand
PublishCourseCommand
ConfigureCourseProblemSetsCommand
```

작성 규칙:

- Controller에서 받은 요청 값을 Application 계층으로 전달하는 목적입니다.
- Request DTO를 Application Service에 직접 넘기지 않습니다.
- Command는 UseCase 실행에 필요한 값만 가집니다.
- Command는 가능하면 Java `record`로 작성합니다.

---

## 8. UseCase 작성 규칙

UseCase는 다음 위치에 둡니다.

```text
course/application/usecase
```

현재 UseCase:

```text
CourseCommandUseCase
CourseQueryUseCase
CourseCategoryQueryUseCase
CourseProblemCommandUseCase
CourseProblemQueryUseCase
```

작성 규칙:

- Controller는 Service 구현체가 아니라 UseCase 인터페이스에 의존합니다.
- 명령 기능과 조회 기능은 가능하면 분리합니다.
- UseCase 메서드명은 기능 목적이 드러나게 작성합니다.

예시:

```text
createCourse()
updateCourse()
publishCourse()
deleteCourse()
findAllCourses()
findCourseById()
```

---

## 9. Application Service 작성 규칙

Application Service는 다음 위치에 둡니다.

```text
course/application/service
```

현재 Service:

```text
CourseCommandService
CourseQueryService
CourseCategoryQueryService
CourseProblemCommandService
CourseProblemQueryService
```

작성 규칙:

- UseCase 인터페이스를 구현합니다.
- 트랜잭션 경계를 가집니다.
- Repository Port를 통해 Domain Model을 조회합니다.
- 필요한 Policy를 호출합니다.
- Domain Model의 행위를 호출합니다.
- 저장은 Repository Port를 통해 수행합니다.

Application Service에서 하지 말아야 할 일:

- Request DTO 의존
- Response DTO 의존
- Controller 의존
- JPA Entity 의존
- Spring Data Repository 직접 의존

---

## 10. Policy 작성 규칙

Policy는 다음 위치에 둡니다.

```text
course/application/policy
```

현재 Policy:

```text
CourseAuthorPolicy
CourseCategoryPolicy
CoursePublishPolicy
CourseProblemPolicy
```

작성 규칙:

- 여러 도메인 조회가 필요한 검증을 담당합니다.
- Application Service의 조건문이 복잡해질 때 Policy로 분리합니다.
- Policy는 필요한 Port 또는 Repository Port를 의존할 수 있습니다.
- 실패 시 Course 도메인 ErrorCode를 사용해 예외를 발생시킵니다.

각 Policy 역할:

| Policy | 역할 |
|---|---|
| `CourseAuthorPolicy` | 강좌 생성자가 활성 운영자인지 검증 |
| `CourseCategoryPolicy` | 활성 카테고리인지 검증 |
| `CoursePublishPolicy` | 강좌 공개 가능 여부 검증 |
| `CourseProblemPolicy` | 강좌 문제세트 연결 요청 검증 |

---

## 11. Port / Adapter 작성 규칙

Port는 다음 위치에 둡니다.

```text
course/application/port
```

Adapter는 다음 위치에 둡니다.

```text
course/infrastructure
```

현재 Port:

```text
UserCatalogPort
LectureCatalogPort
LectureManagementPort
ProblemCatalogPort
```

현재 Adapter:

```text
UserAdapter
LectureCatalogAdapter
LectureManagementAdapter
ProblemCatalogAdapter
```

작성 규칙:

- Course Application 계층은 다른 도메인의 구현체에 직접 의존하지 않습니다.
- 다른 도메인 접근은 Application Port를 통해 수행합니다.
- 실제 구현은 Infrastructure Adapter에서 처리합니다.
- Adapter는 다른 도메인의 Repository 또는 Service를 호출할 수 있습니다.

예시:

```text
CoursePublishPolicy
-> LectureCatalogPort
-> LectureCatalogAdapter
-> LectureRepository
```

---

## 12. Domain Model 작성 규칙

Domain Model은 다음 위치에 둡니다.

```text
course/domain/model
```

현재 Domain Model:

```text
Course
CourseStatus
CourseCategory
CourseCategoryStatus
CourseProblemSet
CourseProblemSetRole
```

작성 규칙:

- 도메인의 상태와 행위를 표현합니다.
- 생성 행위는 정적 팩토리 메서드로 표현할 수 있습니다.
- DB 저장을 위한 JPA 어노테이션을 사용하지 않습니다.
- JPA Entity와 분리합니다.

현재 Course 주요 행위:

```text
Course.create()
Course.update()
Course.publish()
Course.delete()
```

---

## 13. Repository Port 작성 규칙

Repository Port는 다음 위치에 둡니다.

```text
course/domain/repository
```

현재 Repository Port:

```text
CourseRepository
CourseCategoryRepository
CourseProblemSetRepository
```

작성 규칙:

- Domain 계층에는 Repository 인터페이스만 둡니다.
- Spring Data JPA Repository는 Infrastructure에 둡니다.
- Application Service는 Repository Port에만 의존합니다.
- 반환 타입은 JPA Entity가 아니라 Domain Model입니다.

---

## 14. Infrastructure 작성 규칙

Infrastructure는 다음 위치에 둡니다.

```text
course/infrastructure
```

작성 규칙:

- JPA Entity는 `infrastructure/persistence`에 둡니다.
- Spring Data Repository도 `infrastructure/persistence`에 둡니다.
- Repository Adapter는 Domain Repository Port를 구현합니다.
- JPA Entity와 Domain Model 간 변환 메서드를 둡니다.
- 외부 도메인 Adapter는 기능별 하위 패키지에 둡니다.

예시:

```text
course/infrastructure/persistence
course/infrastructure/user
course/infrastructure/lecture
course/infrastructure/problem
```

---

## 15. 예외 처리 규칙

Course 도메인 예외 코드는 다음 위치에서 관리합니다.

```text
course/domain/exception/CourseErrorCode.java
```

작성 규칙:

- Course 도메인에서 발생하는 예외는 `CourseErrorCode`에 추가합니다.
- 에러코드 prefix는 `CRS`를 사용합니다.
- 존재하지 않는 리소스는 `NotFoundException`을 사용합니다.
- 검증 실패는 `ValidationException`을 사용합니다.
- Controller에서 직접 예외 응답을 만들지 않습니다.

예시:

```text
COURSE_NOT_FOUND
COURSE_OPERATOR_REQUIRED
COURSE_CATEGORY_REQUIRED
COURSE_NOT_PUBLISHABLE_STATUS
```

---

## 16. API 응답 규칙

Course API는 공통 응답 형식인 `ApiResponse`를 사용합니다.

성공 응답 예시:

```json
{
  "code": "COURSE_RETRIEVED",
  "message": "강좌 조회 성공",
  "data": {}
}
```

삭제 API는 예외적으로 `204 No Content`를 반환합니다.

```text
DELETE /api/v1/courses/{courseId}
-> 204 No Content
```

---

## 17. 상태 변경 규칙

Course 상태는 다음 enum을 사용합니다.

```text
DRAFT
ACTIVE
INACTIVE
DELETED
```

상태 변경 규칙:

- 강좌 생성 시 기본 상태는 `DRAFT`입니다.
- `ACTIVE` 상태 변경은 강좌 공개 API를 통해서만 수행합니다.
- `DELETED` 상태 변경은 강좌 삭제 API를 통해서만 수행합니다.
- 수정 API에서 `ACTIVE` 또는 `DELETED`로 직접 변경하지 않습니다.
- 공개 가능한 강좌는 `DRAFT` 상태여야 합니다.
- 삭제된 강좌는 일반 조회 대상에서 제외합니다.

---

## 18. CourseProblemSet 규칙

CourseProblemSet은 강좌, 강의, 문제세트 연결 정보를 표현합니다.

역할 enum:

```text
MAIN
FINAL
```

작성 규칙:

- `lectureId`는 필수입니다.
- `problemSetId`는 필수입니다.
- `role`은 필수입니다.
- `displayOrder`는 필수입니다.
- `problemSetId`는 존재하는 문제세트여야 합니다.
- `lectureId`는 해당 강좌에 포함된 강의여야 합니다.

현재 저장 방식:

```text
기존 courseId 연결 전체 삭제
-> 요청 목록 기준 재저장
```

---

## 19. 로깅 규칙

Course 도메인에서는 주요 Controller와 Service 흐름에 로그를 남깁니다.

권장 로그 위치:

- 강좌 생성 요청
- 강좌 수정 요청
- 강좌 공개 요청
- 강좌 삭제 요청
- 강좌 목록 조회
- 강사별 강좌 조회

로그에는 다음 정보를 포함할 수 있습니다.

- `courseId`
- `instructorId`
- `title`
- 처리 결과 count

민감 정보는 로그에 남기지 않습니다.

---

## 20. 테스트 작성 규칙

Course 도메인 테스트 작성 시 다음 흐름을 권장합니다.

- given
- when
- then

예시 테스트 대상:

- 강좌 생성 시 기본 상태가 `DRAFT`인지 검증
- 운영자가 아니면 강좌 생성 실패
- 활성 카테고리가 아니면 강좌 생성 실패
- `DRAFT`가 아니면 강좌 공개 실패
- 강의가 없으면 강좌 공개 실패
- 수정 API에서 `ACTIVE` 직접 변경 실패
- 수정 API에서 `DELETED` 직접 변경 실패
- 강좌 삭제 시 상태가 `DELETED`로 변경되는지 검증

---

## 21. 주의사항

- Controller에서 Repository를 직접 호출하지 않습니다.
- Application Service에서 Request DTO를 직접 받지 않습니다.
- Domain Model에 JPA 어노테이션을 붙이지 않습니다.
- JPA Entity를 Controller 응답으로 반환하지 않습니다.
- 다른 도메인의 Repository를 직접 의존하지 않습니다.
- Course 도메인 구조 변경 시 API 문서와 handoff 문서를 함께 수정합니다.
