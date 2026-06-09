# 코드 작성 컨벤션

> 예외·응답을 코드로 옮길 때의 횡단 규칙 + Swagger 작성 가이드.
> 관련: 에러코드/예외 [`exception.md`](exception.md), 성공 응답 [`response.md`](response.md).

## 1. 횡단 규칙

### 개별 Exception 클래스 신규 생성 금지

도메인마다 예외 클래스를 만들지 말고 공통 예외 타입([`exception.md`](exception.md))에 `XxxErrorCode`를 넘긴다.

```java
// Good
throw new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND);

// Bad — 개별 클래스 만들지 말 것
throw new CourseNotFoundException();
```

### ResponseCode / ResponseMessage 작성 규칙

- 상수명은 동사 없이 행위 결과로 표현: `RETRIEVED`, `CREATED`, `UPDATED`, `DELETED`
- 코드 문자열 포맷: `{DOMAIN}-{ACTION}` (예: `COURSE-RETRIEVED`)
- 생성자 `private` — 인스턴스화 금지

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

## 2. Swagger 작성 가이드

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
