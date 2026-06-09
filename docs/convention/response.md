# 성공 응답 컨벤션 (ApiResponse)

> 성공 응답 포맷·ResponseCode/Message 구조. 결정·이유는 [`docs/adr/0004-error-response-standard.md`](../adr/0004-error-response-standard.md), 마이그레이션은 [`docs/adr/0005-responsedto-migration.md`](../adr/0005-responsedto-migration.md).
> 관련: 에러/예외는 [`exception.md`](exception.md), 코드 작성 규칙(private 생성자·네이밍)은 [`code.md`](code.md).

## 새 컨트롤러 — ApiResponse 사용

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

## 기존 컨트롤러 — ResponseDTO 유지 (점진적 마이그레이션)

기존 컨트롤러는 `ResponseDTO` 패턴 그대로 유지. 새 컨트롤러 작성 시부터 `ApiResponse` 적용.

## ResponseCode / ResponseMessage 파일 위치 및 구조

**각 컨트롤러 패키지에** `XxxResponseCode.java` + `XxxResponseMessage.java` 두 파일을 함께 둔다.

```
domain/course/controller/
├── CourseController.java
├── CourseResponseCode.java     ← 성공 코드 상수
└── CourseResponseMessage.java  ← 성공 메시지 상수
```

작성 규칙(상수 네이밍·private 생성자)은 [`code.md`](code.md) 참고.

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

## 성공 응답 포맷

```json
{
  "timestamp": "2026-05-21T10:00:00Z",
  "status": 200,
  "code": "COURSE-RETRIEVED",
  "message": "강좌 조회에 성공했습니다.",
  "data": { ... }
}
```
