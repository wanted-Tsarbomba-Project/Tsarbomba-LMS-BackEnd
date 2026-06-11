# 개발 완료 기록

> 📌 이 파일은 **끝낸 작업의 일지**예요.
> 작업(이슈)을 하나 완료할 때마다 AI에게 "완료" 또는 "WORKLOG에 기록해줘" 라고 하면 아래 양식으로 정리해줘요.
> 최신 기록이 위로 오도록 **위에 쌓아** 가세요.

---

## 예시 양식

> 아래 내용은 작성 예시이며, 실제 완료 기록은 다음 섹션부터 확인한다.

## [YYYY-MM-DD] #이슈번호 [작업 제목] ✅

### 변경 파일

| 파일 | 변경 |
|------|------|
| `경로/Xxx.java` | 생성 |
| `경로/Yyy.java` | 수정 |

### 주요 작업 내용

- [구현한 기능 요약 1]
- [구현한 기능 요약 2]

### 트러블슈팅

> 발생한 문제 + 원인 + 해결방법 (있을 때만)

- **문제**: [무슨 에러가 났는지]
- **원인**: [왜 발생했는지]
- **해결**: [어떻게 고쳤는지]

### 부수 결정

> 작업 중 내린 판단·컨벤션 (있을 때만)

- [예: 에러 코드는 `도메인-번호` 형식으로 통일]

---

> ✏️ 위 양식을 복사해서 작업마다 새 블록을 **맨 위에** 추가하세요.
> 트러블슈팅·부수 결정은 없으면 생략해도 돼요.

---

## 실제 개발 완료 기록

---

## [2026-06-07] #확인필요 Course / Lecture / Enrollment / Learning API 문서 정리 ✅

### 변경 파일

| 파일 | 변경 |
|------|------|
| `.ai/API.md` | 수정 |
| `.ai/WORKLOG.md` | 수정 |

### 참고 파일

| 파일 | 용도 |
|------|------|
| `src/main/java/com/wanted/codebombalms/course/README.md` | Course API/도메인 구조 확인 |
| `src/main/java/com/wanted/codebombalms/lecture/README.md` | Lecture API/도메인 구조 확인 |
| `src/main/java/com/wanted/codebombalms/enrollment/README.md` | Enrollment API/도메인 구조 확인 |
| `src/main/java/com/wanted/codebombalms/learning/README.md` | Learning API/도메인 구조 확인 |

### 주요 작업 내용

- `course`, `lecture`, `enrollment`, `learning` 담당 범위의 API 목록을 Controller 기준으로 정리함
- 강좌 CRUD, 강의 CRUD, 수강신청, 학습 진행률, 문제세트 학습 흐름 API를 `.ai/API.md` 양식에 맞춰 정리함
- AI 질의 테스트를 위해 미완료/확인 필요 항목을 별도 표로 분리함
- “현재 미완료된 API”, “현재 완료된 작업”, “다음 작업순서” 질문에 답변할 수 있도록 문서 기준 정보를 정리함

### 트러블슈팅

- **문제**: 기존 `.ai/API.md`, `.ai/WORKLOG.md`가 예시 템플릿 상태라 AI가 실제 프로젝트 현황을 답변하기 어려움
- **원인**: 담당 도메인별 구현 API와 완료 작업이 운영 문서에 반영되어 있지 않음
- **해결**: 담당 범위를 `course`, `lecture`, `enrollment`, `learning`으로 좁히고 Controller/README 기준으로 API 현황과 완료 작업을 문서화함

### 부수 결정

- API 구현 여부는 Controller에 엔드포인트가 존재하는지 기준으로 판단함
- 정확한 에러 코드와 Response 상세 필드는 `확인 필요`로 남기고 추후 보완 대상으로 분리함
- 프론트 연동 여부는 코드만으로 확정하기 어려워 `확인 필요` 상태로 분리함

---

## [2026-06-05] #확인필요 도메인별 README 정리 ✅

### 변경 파일

| 파일 | 변경 |
|------|------|
| `src/main/java/com/wanted/codebombalms/course/README.md` | 생성/수정 |
| `src/main/java/com/wanted/codebombalms/lecture/README.md` | 생성/수정 |
| `src/main/java/com/wanted/codebombalms/enrollment/README.md` | 생성/수정 |
| `src/main/java/com/wanted/codebombalms/learning/README.md` | 생성/수정 |

### 주요 작업 내용

- Course 도메인의 강좌 관리, 카테고리, 문제세트 연결 API를 정리함
- Lecture 도메인의 강의 목록/상세/생성/수정/삭제 API를 정리함
- Enrollment 도메인의 수강신청 생성, 조회, 취소 API를 정리함
- Learning 도메인의 강의 진행률, 문제세트 진행률, 관리자용 학습률 조회 API를 정리함

### 부수 결정

- 도메인 README는 AI가 프로젝트 구조를 빠르게 파악하는 참고 문서로 사용함
---

## [2026-06-08] #확인필요 Course INACTIVE 강좌 재활성화 허용 ✅

### 변경 파일

| 파일 | 변경 |
|------|------|
| `src/main/java/com/wanted/codebombalms/course/application/service/CourseCommandService.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/course/domain/exception/CourseErrorCode.java` | 수정 |
| `src/test/java/com/wanted/codebombalms/course/application/service/CourseServiceTest.java` | 수정 |
| `http/course.http` | 수정 |
| `.ai/API.md` | 수정 |

### 주요 작업 내용

- `DRAFT → ACTIVE` 직접 변경은 기존처럼 개설 API 사용을 강제하고, `INACTIVE → ACTIVE` 재활성화는 수정 API에서 허용함
- `CRS-003` 메시지를 작성 중인 강좌 활성화에 한정되도록 조정함
- `CourseServiceTest`에 비활성 강좌 재활성화 성공 케이스를 추가함
- `http/course.http`에 비활성 강좌 재활성화 요청 예시를 추가함

### 트러블슈팅

- **문제**: 기본 `bootRun` 실행 시 `8080` 포트 사용 중으로 애플리케이션 기동 실패
- **원인**: 이미 로컬에서 `8080` 포트를 사용하는 프로세스가 존재함
- **해결**: `--server.port=18080`으로 포트를 변경해 애플리케이션 정상 기동 및 `GET /api/v1/courses` 호출까지 확인함

### 부수 결정

- 최초 개설(`DRAFT → ACTIVE`)은 `PATCH /api/v1/courses/{courseId}/publish`를 유지하고, 운영 중지 후 재노출(`INACTIVE → ACTIVE`)은 `PUT /api/v1/courses/{courseId}` 상태 수정으로 처리함

---

## [2026-06-09] #확인필요 FINAL 문제세트 강좌 단위 연결 정책 반영 ✅

### 변경 파일

| 파일 | 변경 |
|------|------|
| `src/main/java/com/wanted/codebombalms/course/presentation/api/request/CourseProblemSetConfigureRequest.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/course/application/policy/CourseProblemPolicy.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/course/application/service/CourseProblemCommandService.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/course/domain/exception/CourseErrorCode.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/course/infrastructure/persistence/CourseProblemSetJpaEntity.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/course/infrastructure/persistence/SpringDataCourseProblemSetRepository.java` | 수정 |
| `src/test/java/com/wanted/codebombalms/course/controller/CourseProblemControllerTest.java` | 수정 |
| `src/test/java/com/wanted/codebombalms/course/infrastructure/persistence/CourseProblemRepositoryTest.java` | 수정 |
| `.ai/API.md` | 수정 |
| `.ai/STATE.md` | 수정 |
| `.ai/WORKLOG.md` | 수정 |

### 주요 작업 내용

- `FINAL` 문제세트는 강의가 아닌 강좌 전체에 연결되도록 `lectureId = null`을 허용함
- `MAIN`은 기존처럼 `lectureId` 필수, `FINAL`은 `lectureId` 지정 불가로 검증 정책을 분리함
- `lectureId = null`인 `FINAL` 문제세트가 강좌별 조회와 단건 조회에서 누락되지 않도록 쿼리를 수정함
- 기존 저장 로직의 `lectureId` 비교를 null-safe하게 변경함
- 테스트 fixture를 새 정책에 맞게 수정하고, `FINAL` 단건 조회 케이스를 보강함

### 트러블슈팅

- **문제**: `.ai/API.md`가 PowerShell 기본 출력에서 깨져 보여 패치 매칭이 실패함
- **원인**: 파일은 UTF-8 한글이지만 콘솔 출력 인코딩이 맞지 않아 mojibake로 표시됨
- **해결**: 실제 UTF-8 첫 줄 기준으로 문서 상단에 변경사항을 추가함

### 부수 결정

- 운영 DB에는 `lecture_problem_set.lecture_id` nullable 변경이 별도로 필요함
- 다음 단계의 최종 평가 노출 API와 자동 추천 API는 이번 변경 범위에서 분리함

---

## [2026-06-09] #확인필요 학생 본인 수강 목록/취소 me API 추가 ✅

### 변경 파일

| 파일 | 변경 |
|------|------|
| `src/main/java/com/wanted/codebombalms/enrollment/presentation/api/EnrollmentController.java` | 수정 |
| `src/test/java/com/wanted/codebombalms/enrollment/presentation/api/EnrollmentControllerTest.java` | 수정 |
| `.ai/API.md` | 수정 |
| `.ai/STATE.md` | 수정 |
| `.ai/WORKLOG.md` | 수정 |

### 주요 작업 내용

- 프론트가 `userId`를 알 필요 없도록 `GET /api/v1/users/me/enrollments` API를 추가함
- 프론트가 `userId` 없이 수강신청을 취소할 수 있도록 `DELETE /api/v1/users/me/enrollments/{enrollmentId}` API를 추가함
- 수강신청 생성 권한을 `isAuthenticated()`에서 `hasRole('STUDENT')`로 명확히 제한함
- 기존 `{userId}` 기반 조회/취소 API는 호환을 위해 유지함
- `/me` 기반 조회/취소 컨트롤러 테스트를 추가함

### 트러블슈팅

- 특이사항 없음

### 부수 결정

- 학생 본인 기능은 path의 `{userId}`보다 `@AuthenticationPrincipal Long userId` 기반 `/me` API를 우선 사용함
- 관리자/기존 연동 가능성을 고려해 기존 `{userId}` API는 이번 범위에서 제거하지 않음

---

## [2026-06-09] #확인필요 Enrollment me API CodeRabbit 리뷰 대응 ✅

### 변경 파일

| 파일 | 변경 |
|------|------|
| `src/main/java/com/wanted/codebombalms/enrollment/presentation/api/EnrollmentController.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/enrollment/application/usecase/EnrollmentQueryUseCase.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/enrollment/application/service/EnrollmentQueryService.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/enrollment/application/query/MyCourseResult.java` | 생성 |
| `src/main/java/com/wanted/codebombalms/enrollment/presentation/api/response/MyCourseResponse.java` | 수정 |
| `src/test/java/com/wanted/codebombalms/enrollment/presentation/api/EnrollmentControllerTest.java` | 수정 |
| `src/test/java/com/wanted/codebombalms/enrollment/application/service/EnrollmentServiceTest.java` | 수정 |
| `.ai/WORKLOG.md` | 수정 |

### 주요 작업 내용

- 기존 `DELETE /api/v1/users/{userId}/enrollments/{enrollmentId}`의 권한을 학생 본인 검증 조건으로 강화함
- 내 수강 목록 응답 조립을 컨트롤러에서 애플리케이션 계층 결과 모델(`MyCourseResult`)로 이동함
- `/me` 조회/취소 및 수강신청 생성 API의 학생 권한 실패 테스트를 추가함
- 컨트롤러 테스트에서 메서드 보안이 실제로 동작하도록 테스트 전용 Security 설정을 적용함

### 트러블슈팅

- **문제**: `@WebMvcTest`에서 `@AutoConfigureMockMvc(addFilters = false)` 상태라 `@PreAuthorize` 실패 테스트가 실제 권한 검증을 타지 않음
- **원인**: 테스트 슬라이스에서 필터와 메서드 보안 경계가 비활성화되어 컨트롤러 메서드가 그대로 실행됨
- **해결**: 테스트 전용 `@EnableMethodSecurity`와 `SecurityFilterChain`을 추가하고 요청별 `authentication(...)`으로 인증 객체를 주입함

### 부수 결정

- 기존 `{userId}` 기반 취소 API는 삭제하지 않고, 호환성을 유지하되 `hasRole('STUDENT') and #userId == authentication.principal`로 우회 가능성을 차단함
- `findAllEnrollments` 관리자/운영자 조회는 기존 응답 조립 구조를 유지하고, 학생 본인 조회 흐름만 애플리케이션 계층 결과 모델을 사용함
---

## [2026-06-10] #347 learning 도메인 외부 BC 조회 Adapter 의존성 개선 완료

### 변경 파일

| 파일 | 변경 |
|------|------|
| `src/main/java/com/wanted/codebombalms/learning/infrastructure/course/LearningCourseAdapter.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/learning/infrastructure/lecture/LearningLectureAdapter.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/learning/infrastructure/enrollment/LearningEnrollmentAdapter.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/enrollment/application/usecase/EnrollmentQueryUseCase.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/enrollment/application/service/EnrollmentQueryService.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/enrollment/domain/repository/EnrollmentRepository.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/enrollment/infrastructure/persistence/EnrollmentRepositoryAdapter.java` | 수정 |
| `.ai/WORKLOG.md` | 수정 |

### 주요 작업 내용

- `LearningCourseAdapter`가 `CourseJpaEntity`, `SpringDataCourseRepository`를 직접 참조하지 않고 `CourseQueryUseCase`를 통해 활성 강좌 정보를 조회하도록 변경
- `LearningLectureAdapter`가 `LectureJpaEntity`, `SpringDataLectureRepository`를 직접 참조하지 않고 `LectureQueryUseCase`를 통해 강의 존재 여부, 강좌 ID, 강의 목록을 조회하도록 변경
- `LearningEnrollmentAdapter`가 `EnrollmentJpaEntity`, `SpringDataEnrollmentRepository`를 직접 참조하지 않고 `EnrollmentQueryUseCase`를 통해 활성 수강생 ID 목록을 조회하도록 변경
- `EnrollmentQueryUseCase`에 강좌별 활성 수강생 ID 조회 메서드를 추가하고, `EnrollmentRepository` 내부 조회 메서드로 위임하도록 구현

### 트러블슈팅

- **문제**: `learning`에서 `enrollment` 조회를 UseCase로 넘기려 했지만 기존 `EnrollmentQueryUseCase`에 강좌별 활성 수강생 ID 조회 계약이 없었음
- **원인**: 기존 수강 조회 UseCase는 내 수강 목록/전체 활성 수강 목록 중심으로만 구성되어 있었음
- **해결**: `EnrollmentQueryUseCase#findActiveStudentIdsByCourse`와 `EnrollmentRepository#findByCourseIdAndStatus`를 추가해 `learning`은 UseCase만 의존하고, 실제 DB 조회는 `enrollment` 도메인 내부에서 처리하도록 분리

### 부수 결정

- 상대 도메인 NotFound 예외가 그대로 노출되지 않도록 `learning` Adapter에서 필요한 경우 `LearningErrorCode` 기반 예외로 변환
- 전체 활성 수강 목록을 가져와 필터링하지 않고, 강좌 ID와 상태 조건을 Repository 계약으로 제공해 조회 범위를 줄임
- 검증: `./gradlew.bat test` 성공

## [2026-06-11] #348 learning 문제 연결 조회 Adapter 의존성 개선 완료

### 변경 파일

| 파일 | 변경 |
|------|------|
| `src/main/java/com/wanted/codebombalms/course/application/usecase/CourseProblemQueryUseCase.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/course/application/service/CourseProblemQueryService.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/learning/infrastructure/course/LearningCourseProblemAdapter.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/learning/infrastructure/course/LearningLectureProblemSetAdapter.java` | 수정 |
| `.ai/WORKLOG.md` | 수정 |

### 주요 작업 내용

- `LearningCourseProblemAdapter`의 `SpringDataCourseProblemSetRepository`, `CourseProblemSetJpaEntity` 직접 의존을 제거하고 `CourseProblemQueryUseCase`를 통해 조회하도록 변경
- `CourseProblemQueryUseCase`에 역할별 문제 연결 목록 조회와 단건 문제 연결 조회 계약 추가
- `LearningLectureProblemSetAdapter`의 `CourseProblemSetRepository`, `SpringDataProblemRepository` 직접 의존 제거
- 문제 연결 단건 조회는 `CourseProblemQueryUseCase`, 문제의 문제 세트 소속 확인은 기존 `ProblemQueryService`의 공개 조회 메서드를 사용하도록 변경
- course 문제 연결 조회 실패를 `LRN-005`로 변환하면서 원인 예외를 보존

### 트러블슈팅

- **문제**: problems 도메인에 새로운 UseCase를 추가하면 담당 도메인 범위를 벗어남
- **원인**: 문제 소속 확인을 위한 별도 Application 계약을 새로 만들려 했음
- **해결**: problems 도메인은 수정하지 않고 기존 `ProblemQueryService#findProblemForSubmission`을 활용해 소속 여부를 판별

### 부수 결정

- 변경 범위를 담당 도메인인 `course`, `learning`으로 제한
- 문제 미존재 시 기존 동작과 동일하게 `false`를 반환해 learning 서비스의 `LRN-006` 처리 흐름 유지
- 검증: `./gradlew.bat test` 성공
---

## [2026-06-11] #348 CodeRabbit 문제 존재성 조회 리뷰 대응 완료

### 변경 파일

| 파일 | 변경 |
|------|------|
| `src/main/java/com/wanted/codebombalms/course/application/port/ProblemCatalogPort.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/course/infrastructure/problem/ProblemCatalogAdapter.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/learning/application/port/LearningProblemPort.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/learning/application/service/LectureProblemSetService.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/learning/domain/exception/LearningErrorCode.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/learning/infrastructure/problem/LearningProblemAdapter.java` | 수정 |
| `.ai/API.md` | 수정 |
| `.ai/WORKLOG.md` | 수정 |

### 주요 작업 내용

- `LearningProblemAdapter`의 문제 존재성 확인을 기존 `ProblemCatalogPort`에 위임해 중복 구현 제거
- `ProblemCatalogAdapter`의 문제 존재/소속 확인을 count 기반 JPQL로 변경해 TEXT 필드를 포함한 엔티티 전체 로드 제거
- 문제 자체 미존재와 문제 세트 불일치를 각각 `LRN-007`, `LRN-006`으로 구분
- `.ai/API.md` 문제 제출 오류 코드에 `LRN-007` 추가

### 부수 결정

- problems 도메인은 수정하지 않고 담당 도메인인 course/learning 내부에서 해결
- 검증: `./gradlew.bat test` 성공

---

## [2026-06-11] #349 lecture/enrollment CourseJpaEntity 직접 참조 개선 완료

### 작업 정보

- 브랜치: `refactor/lecture`
- 상태: ✅ 완료

### 변경 파일

| 파일 | 변경 |
|------|------|
| `src/main/java/com/wanted/codebombalms/lecture/infrastructure/persistence/LectureJpaEntity.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/lecture/infrastructure/persistence/LectureRepositoryAdapter.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/lecture/infrastructure/persistence/SpringDataLectureRepository.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/enrollment/infrastructure/persistence/EnrollmentJpaEntity.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/enrollment/infrastructure/persistence/EnrollmentRepositoryAdapter.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/enrollment/infrastructure/persistence/SpringDataEnrollmentRepository.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/course/infrastructure/persistence/SpringDataCourseRepository.java` | 수정 |
| `src/test/java/com/wanted/codebombalms/lecture/infrastructure/persistence/LectureRepositoryTest.java` | 수정 |
| `src/test/java/com/wanted/codebombalms/course/infrastructure/persistence/CourseProblemRepositoryTest.java` | 수정 |
| `src/test/java/com/wanted/codebombalms/course/infrastructure/persistence/CourseRepositoryTest.java` | 수정 |
| `.ai/WORKLOG.md` | 수정 |

### 주요 작업 내용

- `LectureJpaEntity`, `EnrollmentJpaEntity`의 `CourseJpaEntity` 연관관계를 `courseId` 값 매핑으로 변경
- 두 Repository Adapter의 `SpringDataCourseRepository` 직접 의존 제거
- 강의 도메인 복원에 필요한 강좌 정보는 `CourseCatalogPort`를 통해 조회하도록 변경
- 강좌별 강의 목록 조회 시 강좌를 한 번만 조회하고, 전체 목록 조회 시 강좌별 로컬 캐시를 사용하도록 보완
- scalar `courseId` 필드에 맞게 Spring Data 파생 쿼리와 강좌 하드 딜리트 JPQL 경로 수정

### 트러블슈팅

- **문제**: 엔티티 필드 변경 후 강좌 하드 딜리트 JPQL이 기존 `course.courseId` 경로를 참조해 컨텍스트 로딩 실패
- **원인**: `LectureJpaEntity`, `EnrollmentJpaEntity`의 연관관계가 scalar `courseId`로 변경됐지만 정리 쿼리 경로가 남아 있었음
- **해결**: 관련 JPQL을 `lecture.courseId`, `enrollment.courseId` 기준으로 변경
- **문제**: JPA 슬라이스 테스트에서 `LectureRepositoryAdapter`의 `CourseCatalogPort` 빈 누락
- **해결**: 해당 테스트 구성에 `CourseCatalogAdapter`를 추가

### 부수 결정

- API와 `Lecture` 도메인 모델 계약은 유지하고 persistence 계층의 직접 JPA 결합만 제거
- 담당 도메인인 course, lecture, enrollment 범위 안에서만 수정
- 검증: `./gradlew.bat test` 성공

---

## [2026-06-11] #349 CodeRabbit 강좌 참조 조회 리뷰 대응 완료

### 변경 파일

| 파일 | 변경 |
|------|------|
| `src/main/java/com/wanted/codebombalms/course/domain/repository/CourseRepository.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/course/infrastructure/persistence/CourseRepositoryAdapter.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/course/infrastructure/persistence/SpringDataCourseRepository.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/enrollment/infrastructure/persistence/EnrollmentJpaEntity.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/lecture/application/port/CourseCatalogPort.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/lecture/infrastructure/course/CourseCatalogAdapter.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/lecture/infrastructure/persistence/LectureJpaEntity.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/lecture/infrastructure/persistence/LectureRepositoryAdapter.java` | 수정 |
| `.ai/STATE.md` | 수정 |
| `.ai/WORKLOG.md` | 수정 |

### 주요 작업 내용

- 강의 전체 조회에서 고유 강좌별 단건 조회가 반복되지 않도록 `CourseCatalogPort`에 일괄 조회 계약 추가
- course Repository에 `courseId IN (...)` 기반 활성 강좌 일괄 조회 구현 추가
- 일괄 조회 결과에 요청한 강좌가 누락되면 기존 `COURSE_NOT_FOUND` 예외를 유지하도록 정합성 검증
- `lecture.course_id`, `enrollment.course_id` 조회 인덱스를 JPA 테이블 메타데이터에 명시

### 부수 결정

- 단건 조회 구현은 이미 `null` 대신 `NotFoundException`을 보장하므로 별도 `IllegalStateException` 래핑은 적용하지 않음
- scalar ID 매핑을 유지하기 위해 `CourseJpaEntity` 연관관계를 다시 추가하지 않음
- 운영 DB FK는 코드 저장소에서 확인할 수 없어 별도 스키마 확인 항목으로 백로그 등록
- 검증: `./gradlew.bat test` 성공

---

## [2026-06-11] #394 강의/일반 문제풀이 상태 분리 구현 완료

### 작업 정보

- 브랜치: `refactor/lecture`
- 상태: ✅ 구현 및 전체 테스트 완료

### 변경 파일

| 파일 | 변경 |
|------|------|
| `src/main/java/com/wanted/codebombalms/learning/application/port/LearningProblemPort.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/learning/application/port/LearningProblemGradingPort.java` | 생성 |
| `src/main/java/com/wanted/codebombalms/learning/application/service/LectureProblemSetService.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/learning/domain/exception/LearningErrorCode.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/learning/domain/model/LectureProblemSubmission.java` | 생성 |
| `src/main/java/com/wanted/codebombalms/learning/domain/repository/LectureProblemSubmissionRepository.java` | 생성 |
| `src/main/java/com/wanted/codebombalms/learning/infrastructure/problem/LearningProblemAdapter.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/learning/infrastructure/problem/LearningProblemGradingAdapter.java` | 생성 |
| `src/main/java/com/wanted/codebombalms/learning/infrastructure/persistence/LectureProblemSubmissionJpaEntity.java` | 생성 |
| `src/main/java/com/wanted/codebombalms/learning/infrastructure/persistence/LectureProblemSubmissionRepositoryAdapter.java` | 생성 |
| `src/main/java/com/wanted/codebombalms/learning/infrastructure/persistence/SpringDataLectureProblemSubmissionRepository.java` | 생성 |
| `src/main/java/com/wanted/codebombalms/course/infrastructure/persistence/SpringDataCourseRepository.java` | 수정 |
| `src/main/java/com/wanted/codebombalms/lecture/infrastructure/persistence/SpringDataLectureRepository.java` | 수정 |
| `src/main/resources/db/schema/lecture_problem_submission.sql` | 생성 |
| `src/test/java/com/wanted/codebombalms/learning/application/service/LectureProblemSetServiceTest.java` | 생성 |
| `src/test/java/com/wanted/codebombalms/course/infrastructure/persistence/CourseRepositoryTest.java` | 수정 |
| `src/test/java/com/wanted/codebombalms/lecture/infrastructure/persistence/LectureRepositoryTest.java` | 수정 |
| `.ai/API.md` | 수정 |
| `.ai/STATE.md` | 수정 |
| `.ai/WORKLOG.md` | 수정 |

### 주요 작업 내용

- 강의 문제 입장·진행 조회에서 일반 `EnterProblemSetUseCase`, `GetProblemProgressUseCase` 의존 제거
- 강의 문제 제출에서 일반 `SubmissionCommandUseCase` 호출을 제거해 `problem_progress`와 일반 제출 상태 갱신 차단
- 문제 콘텐츠와 채점 기능만 외부 조회 계약을 통해 재사용하고, 제출 결과는 `lecture_problem_submission`에 별도 저장
- 문제 상태, 최근 제출 ID, 풀이 수와 다음 문제 개방을 강의 전용 진행·제출 데이터로 계산
- 강좌·강의 하드 딜리트 시 강의 전용 제출을 먼저 삭제하도록 기존 정리 순서 보완

### 트러블슈팅

- **문제**: 강의 API가 분리되어 있어도 내부에서 일반 문제 입장·제출 UseCase를 호출해 일반 진행 상태가 함께 변경됨
- **원인**: 강의 진행 테이블은 존재했지만 문제별 상태와 최근 제출을 일반 `problem_progress`, `submission`에서 조회하고 있었음
- **해결**: Learning 내부에 강의 전용 제출 저장소를 추가하고 모든 상태 계산을 강의 전용 데이터로 전환

### 부수 결정

- 외부 `problems`, `submission` 도메인 코드는 수정하지 않고 담당 도메인 내부 어댑터에서 콘텐츠와 채점 기능만 사용
- 강의 제출에서는 일반 문제풀이 포인트 이벤트를 발행하지 않으며 응답의 `earnedPoint=0`, `pointGranted=false`를 반환
- API URL과 기존 응답 필드 구조는 유지
- 로컬 프로필이 `ddl-auto: validate`이므로 개발 DB에는 제공한 DDL을 별도로 적용해야 함
- 검증: `./gradlew.bat test` 성공

---

## [2026-06-11] #394 CodeRabbit 강의 제출 정합성 리뷰 대응 완료

### 주요 작업 내용

- 강의 문제 진행 행에 비관적 쓰기 잠금을 적용해 동일 사용자의 동시 제출 시도 번호 계산을 직렬화
- `attemptLimit` 도달 후 채점과 제출 저장이 진행되지 않도록 `SUB-004` 예외 처리 추가
- 최신 제출 조회에 제출 ID 역순 보조 정렬을 추가해 동일 시각 데이터의 순서를 안정화
- 문제 세트의 시작 코드를 한 번만 조회해 소문제별 반복 조회 제거
- 오답, 시도 횟수 초과, 채점 실패 시나리오 테스트 추가

### 부수 결정

- 외래 키와 유니크 제약 등 DDL 변경은 이번 리뷰 대응 범위에서 제외
- #394와 무관한 course/category seed SQL 피드백은 반영하지 않음
- 검증: `./gradlew.bat test` 성공
