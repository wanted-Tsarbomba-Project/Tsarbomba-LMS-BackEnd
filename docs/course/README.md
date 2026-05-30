# Course 도메인 문서

## 1. 문서 목적

이 문서는 Code-Bomba-LMS 프로젝트의 Course 도메인 구조와 주요 기능을 정리하기 위한 문서입니다.

Course 도메인은 강좌 생성, 수정, 조회, 공개, 삭제와 강좌 카테고리 조회, 강좌-문제세트 연결을 담당합니다.

본 문서는 다음 내용을 빠르게 확인하기 위한 용도로 작성합니다.

- Course 도메인의 역할
- Course 도메인 관련 문서 목록
- 주요 기능 요약
- 패키지 구조 요약
- 연관 도메인
- 작업 시 참고 사항

---

## 2. Course 도메인 역할

Course 도메인은 LMS에서 학습의 기준이 되는 강좌 정보를 관리합니다.

주요 책임은 다음과 같습니다.

- 강좌 생성
- 강좌 목록 조회
- 강좌 상세 조회
- 카테고리별 강좌 조회
- 강사별 강좌 조회
- 강좌 수정
- 강좌 공개 처리
- 강좌 삭제
- 강좌 카테고리 조회
- 강좌와 문제세트 연결 관리

Course는 Lecture, Enrollment, Learning, Problems 도메인과 연결되는 기준 도메인입니다.

---

## 3. 문서 목록

| 문서 | 설명 |
|---|---|
| `README.md` | Course 도메인 전체 개요 |
| `api-spec.md` | Course 관련 API 명세 |
| `clean_architecture_plan.md` | Course 계층 구조와 의존성 방향 |
| `convention.md` | Course 도메인 개발 규칙 |
| `handoff.md` | Course 도메인 인수인계 및 작업 메모 |

---

## 4. 주요 기능

| 기능 | 설명 |
|---|---|
| 강좌 생성 | 운영자가 새로운 강좌를 생성합니다. |
| 강좌 조회 | 강좌 목록, 강좌 상세, 카테고리별 강좌를 조회합니다. |
| 강사별 강좌 조회 | 특정 강사가 생성한 강좌 목록을 조회합니다. |
| 강좌 수정 | 강좌 제목, 설명, 카테고리, 썸네일, 상태를 수정합니다. |
| 강좌 공개 | DRAFT 상태의 강좌를 ACTIVE 상태로 공개합니다. |
| 강좌 삭제 | 강좌를 Soft Delete 처리합니다. |
| 강좌 카테고리 조회 | 활성화된 강좌 카테고리 목록을 조회합니다. |
| 강좌 문제세트 연결 | 강좌와 강의에 문제세트를 연결합니다. |

---

## 5. 패키지 구조 요약

```text
src/main/java/com/wanted/codebombalms/course/
 ├─ presentation/
 ├─ application/
 ├─ domain/
 └─ infrastructure/
```

Course 도메인은 Clean Architecture 흐름에 맞춰 다음 계층으로 구성됩니다.

| 계층 | 역할 |
|---|---|
| `presentation` | API 요청/응답 처리 |
| `application` | UseCase 흐름 조립, 트랜잭션 처리, 정책 검증 |
| `domain` | Course 도메인 모델, Repository Port, 도메인 예외 |
| `infrastructure` | JPA Entity, Repository Adapter, 외부 도메인 Adapter |

---

## 6. 연관 도메인

| 연관 도메인 | 연관 이유 |
|---|---|
| User | 강좌 생성자가 운영자인지 확인합니다. |
| Lecture | 강좌 공개 전 강의 존재 여부를 확인하고, 강좌 삭제 시 연결 강의를 처리합니다. |
| Problems | 강좌에 연결할 문제세트 존재 여부를 확인합니다. |
| Enrollment | 강좌 수강 신청 정보와 연결됩니다. |
| Learning | 강좌 학습 진행률 정보와 연결됩니다. |

---

## 7. 주요 API 요약

| 기능 | Method | URL |
|---|---|---|
| 강좌 목록 조회 | GET | `/api/v1/courses` |
| 카테고리별 강좌 조회 | GET | `/api/v1/course-categories/{courseCategoryId}/courses` |
| 강좌 상세 조회 | GET | `/api/v1/courses/{courseId}` |
| 강좌 생성 | POST | `/api/v1/courses` |
| 강좌 수정 | PUT | `/api/v1/courses/{courseId}` |
| 강좌 공개 | PATCH | `/api/v1/courses/{courseId}/publish` |
| 강좌 삭제 | DELETE | `/api/v1/courses/{courseId}` |
| 강좌 카테고리 조회 | GET | `/api/v1/course-categories` |
| 강좌 문제세트 조회 | GET | `/api/v1/courses/{courseId}/problem-sets` |
| 강의 문제세트 조회 | GET | `/api/v1/lectures/{lectureId}/problem-sets` |
| 강좌 문제세트 연결 | PUT | `/api/v1/courses/{courseId}/problem-sets` |

자세한 API 명세는 `api-spec.md`를 참고합니다.

---

## 8. 구현 시 핵심 규칙

- Controller에서 Repository를 직접 호출하지 않습니다.
- Controller에서 복잡한 비즈니스 규칙을 직접 검증하지 않습니다.
- Request DTO는 Presentation 계층에 둡니다.
- Command는 Application 계층에 둡니다.
- JPA Entity는 Infrastructure 계층에 둡니다.
- Domain Model과 JPA Entity를 분리합니다.
- 다른 도메인의 Repository를 직접 의존하지 않고 Port를 통해 접근합니다.
- 강좌 공개 가능 여부는 `CoursePublishPolicy`에서 검증합니다.
- 강좌 생성자의 운영자 여부는 `CourseAuthorPolicy`에서 검증합니다.
- 활성 카테고리 여부는 `CourseCategoryPolicy`에서 검증합니다.

---

## 9. 참고 사항

Course 도메인은 현재 프로젝트의 구조 통일 기준에 비교적 잘 맞춰져 있습니다.

```text
course/
 ├─ presentation/
 ├─ application/
 ├─ domain/
 └─ infrastructure/
```

다른 도메인 README를 작성할 때 Course 도메인 문서를 기준 예시로 사용할 수 있습니다.
