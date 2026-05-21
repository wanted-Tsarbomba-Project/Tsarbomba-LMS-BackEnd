# API 응답 / 예외 처리 / 에러코드 컨벤션

`ref/global2` 브랜치에서 도입된 글로벌 공통 구조에 대한 팀 컨벤션 문서입니다.

---

## 1. 에러코드 네이밍

### 포맷
```
{DOMAIN}-{NNN}          // 단일 도메인
{DOMAIN}-{SUBDOMAIN}-{NNN}  // 하위 도메인 있는 경우
```

### 코드표

| 도메인 | 접두어 | 예시 |
|--------|--------|------|
| `course` | `CRS` | `CRS-001` |
| `lecture` | `LCT` | `LCT-001` |
| `enrollment` | `ENR` | `ENR-001` |
| `auth` | `AUT` | `AUT-001` |
| `user` | `USR` | `USR-001` |
| `submission` | `SUB` | `SUB-001` |
| `problems` (공통) | `PRB` | `PRB-001` |
| `problems.problem` | `PRB-PBL` | `PRB-PBL-001` |
| `problems.set` | `PRB-SET` | `PRB-SET-001` |
| `problems.category` | `PRB-CAT` | `PRB-CAT-001` |
| `problems.hint` | `PRB-HNT` | `PRB-HNT-001` |
| `problems.dataset` | `PRB-DAT` | `PRB-DAT-001` |
| `problems.progress` | `PRB-PRG` | `PRB-PRG-001` |
| `problems.result` | `PRB-RES` | `PRB-RES-001` |
| `admin.operation.rule` | `ADM-ARL` | `ADM-ARL-001` |
| `admin.operation.alert` | `ADM-ALT` | `ADM-ALT-001` |

### 에러코드 추가 방법
1. 해당 도메인의 `XxxErrorCode` enum에 상수 추가
2. 코드 문자열은 위 코드표 prefix + 시퀀스 번호 순서대로
3. **개별 Exception 클래스 신규 생성 금지** — 공통 예외 타입 사용

```java
// Good
throw new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND);

// Bad — 개별 클래스 만들지 말 것
throw new CourseNotFoundException();
```

---

## 2. 예외 처리 구조

### 공통 예외 타입 (global.domain.common.error.exception)

| 클래스 | HTTP Status | 용도 |
|--------|-------------|------|
| `NotFoundException` | 404 | 리소스 없음 |
| `ValidationException` | 400 | 입력값 검증 실패 |
| `UnauthorizedException` | 401 | 인증 실패 |
| `ForbiddenException` | 403 | 권한 없음 |
| `ConflictException` | 409 | 중복 / 충돌 |

### 사용 예시

```java
// 조회 실패
courseRepository.findById(id)
    .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

// 중복
if (exists) throw new ConflictException(EnrollmentErrorCode.DUPLICATE_ENROLLMENT);

// 입력값 검증
if (title == null) throw new ValidationException(ProblemErrorCode.PROBLEM_SET_TITLE_REQUIRED);
```

### 에러 응답 포맷 (ApiErrorResponse)

```json
{
  "timestamp": "2026-05-21T10:00:00Z",
  "status": 404,
  "code": "CRS-001",
  "message": "존재하지 않는 강좌입니다.",
  "path": "/api/v1/courses/999"
}
```

---

## 3. 성공 응답 포맷 (ApiResponse)

### 새 컨트롤러 — ApiResponse 사용

```java
// 조회 / 수정 (200)
return ResponseEntity.ok(ApiResponse.success(
    CourseResponseCode.RETRIEVED,
    CourseResponseMessage.RETRIEVED,
    response
));

// 생성 (201)
return ResponseEntity.status(201).body(ApiResponse.created(
    CourseResponseCode.CREATED,
    CourseResponseMessage.CREATED,
    response
));

// 삭제 (204) — ApiResponse 사용하지 않음
return ResponseEntity.noContent().build();
```

### 기존 컨트롤러 — ResponseDTO 유지 (점진적 마이그레이션)

기존 컨트롤러는 `ResponseDTO` 패턴 그대로 유지. 새 컨트롤러 작성 시부터 `ApiResponse` 적용.

### ResponseCode / ResponseMessage 파일 위치 및 구조

**각 컨트롤러 패키지에** `XxxResponseCode.java` + `XxxResponseMessage.java` 두 파일을 함께 둔다.

```
domain/course/controller/
├── CourseController.java
├── CourseResponseCode.java     ← 성공 코드 상수
└── CourseResponseMessage.java  ← 성공 메시지 상수
```

**작성 규칙:**
- 상수명은 동사 없이 행위 결과로 표현: `RETRIEVED`, `CREATED`, `UPDATED`, `DELETED`
- 코드 문자열 포맷: `{DOMAIN}-{ACTION}` (예: `COURSE-RETRIEVED`)
- 생성자 `private` — 인스턴스화 금지

**예시 (CourseResponseCode / CourseResponseMessage):**

```java
public class CourseResponseCode {
    private CourseResponseCode() {}

    public static final String RETRIEVED = "COURSE-RETRIEVED";
    public static final String CREATED   = "COURSE-CREATED";
    public static final String UPDATED   = "COURSE-UPDATED";
    public static final String DELETED   = "COURSE-DELETED";
}

public class CourseResponseMessage {
    private CourseResponseMessage() {}

    public static final String RETRIEVED = "강좌 조회에 성공했습니다.";
    public static final String CREATED   = "강좌가 생성되었습니다.";
    public static final String UPDATED   = "강좌가 수정되었습니다.";
    public static final String DELETED   = "강좌가 삭제되었습니다.";
}
```

### 도메인별 코드/메시지 파일 현황

| 도메인 | ResponseCode | ResponseMessage |
|--------|-------------|-----------------|
| course | `CourseResponseCode` | `CourseResponseMessage` |
| lecture | `LectureResponseCode` | `LectureResponseMessage` |
| enrollment | `EnrollmentResponseCode` | `EnrollmentResponseMessage` |
| submission | `SubmissionResponseCode` | `SubmissionResponseMessage` |
| problems.set | `ProblemSetResponseCode` | `ProblemSetResponseMessage` |
| problems.category | `ProblemCategoryResponseCode` | `ProblemCategoryResponseMessage` |
| problems.dataset | `ProblemDatasetResponseCode` | `ProblemDatasetResponseMessage` |
| problems.hint | `ProblemHintResponseCode` | `ProblemHintResponseMessage` |
| problems.problem | `ProblemResponseCode` | `ProblemResponseMessage` |
| problems.progress | `ProgressResponseCode` | `ProgressResponseMessage` |
| problems.result | `ResultResponseCode` | `ResultResponseMessage` |
| admin.operation.rule | `AutomationRuleResponseCode` | `AutomationRuleResponseMessage` |
| admin.operation.alert | `OperationAlertResponseCode` | `OperationAlertResponseMessage` |

### 성공 응답 포맷

```json
{
  "timestamp": "2026-05-21T10:00:00Z",
  "status": 200,
  "code": "COURSE-RETRIEVED",
  "message": "강좌 조회에 성공했습니다.",
  "data": { ... }
}
```

---

## 4. Swagger 작성 가이드

의존성: `springdoc-openapi-starter-webmvc-ui:2.8.6`
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### 새 엔드포인트 작성 예시

```java
@Operation(summary = "강좌 단건 조회")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "조회 성공"),
    @ApiResponse(responseCode = "404", description = "CRS-001: 존재하지 않는 강좌")
})
@GetMapping("/{courseId}")
public ResponseEntity<ApiResponse<CourseDetailResponse>> findCourse(@PathVariable Long courseId) { ... }
```

### 규칙
- 공통 에러(`401`, `403`, `500`)는 생략 가능 — `GlobalExceptionHandler`에서 자동 처리
- 도메인별 에러만 `@ApiResponse`로 명시
- `description`에 에러코드 포함: `"CRS-001: 존재하지 않는 강좌"`
