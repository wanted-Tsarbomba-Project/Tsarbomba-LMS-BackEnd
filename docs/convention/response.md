# 성공 응답 컨벤션 (ApiResponse)

> 성공 응답 포맷·ResponseCode/Message 규칙. 결정·이유는 [`docs/adr/0004-error-response-standard.md`](../adr/0004-error-response-standard.md).
> 관련: 에러/예외는 [`exception.md`](exception.md), 코드 작성 규칙(private 생성자·네이밍)은 [`code.md`](code.md).

> 모든 컨트롤러가 `ApiResponse`로 통일됨(ResponseDTO 마이그레이션 완료). 신규 컨트롤러도 `ApiResponse`만 쓴다.

## 사용법

`ApiResponse`(`global.presentation.api.common`) 정적 팩토리:

| 메서드 | 용도 |
|--------|------|
| `success(code, message, data)` | 조회·수정 (200, 데이터 있음) |
| `success(code, message)` | 데이터 없는 200 |
| `created(code, message, data)` | 생성 (201) |

```java
// 조회 / 수정 (200)
return ResponseEntity.ok(ApiResponse.success(
    CourseResponseCode.RETRIEVED,
    CourseResponseMessage.RETRIEVED,
    response
));

// 생성 (201)
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
    CourseResponseCode.CREATED,
    CourseResponseMessage.CREATED,
    response
));

// 삭제 (204) — ApiResponse 쓰지 않음
return ResponseEntity.noContent().build();   // ResponseEntity<Void>
```

## ResponseCode / ResponseMessage

각 도메인 `presentation/api/` 패키지에 `XxxResponseCode` + `XxxResponseMessage` 두 클래스를 둔다(상수 모음).

```
course/presentation/api/
├── CourseController.java
├── CourseResponseCode.java     ← 성공 코드 상수
└── CourseResponseMessage.java  ← 성공 메시지 상수
```

작성 규칙(상수 네이밍·private 생성자)은 [`code.md`](code.md).

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
