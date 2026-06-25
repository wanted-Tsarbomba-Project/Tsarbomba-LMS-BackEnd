# Course Domain

`course` 도메인은 LMS에서 강의 상품의 상위 단위인 강좌를 관리한다. 강좌 목록/상세 조회, 강좌 생성/수정/공개/삭제, 카테고리 조회, 강사별 강좌 조회를 담당한다.

## 주요 역할

- 수강생에게 공개된 활성 강좌 목록과 상세 정보를 제공한다.
- 운영자에게 초안, 활성, 삭제 전 상태를 포함한 강좌 관리 기능을 제공한다.
- 강좌 카테고리 기준으로 강좌를 필터링한다.
- 강좌 썸네일 이미지를 업로드하고 저장 경로를 반환한다.
- 기존 강좌 문제세트 REST API를 제공하고 실제 연결 처리는 `lecture` 유스케이스에 위임한다.
- 특정 강사에게 연결된 강좌 목록을 조회한다.

## 패키지 구조

```text
course
├── application
│   ├── command      # 강좌 생성, 수정, 공개 명령 객체
│   ├── policy       # 강사 권한, 카테고리, 공개 조건 검증
│   ├── port         # lecture, problem 등 외부 도메인 연동 포트
│   ├── service      # 유스케이스 구현체
│   └── usecase      # presentation 계층이 의존하는 입력 포트
├── domain
│   ├── exception    # CourseErrorCode
│   ├── model        # Course, CourseCategory 등
│   └── repository   # 도메인 저장소 인터페이스
├── infrastructure
│   ├── lecture      # lecture 도메인 연동 어댑터
│   ├── persistence  # JPA 엔티티, Spring Data Repository, 어댑터
│   └── problem      # problem set 연동 어댑터
└── presentation
    └── api          # REST Controller, request/response DTO
```

## 주요 모델

| 모델 | 설명 |
| --- | --- |
| `Course` | 강좌의 기본 정보, 강사 ID, 카테고리, 썸네일, 상태, 삭제 시각을 가진 중심 모델 |
| `CourseCategory` | 강좌 분류 정보와 활성/비활성 상태를 표현 |
| `CourseStatus` | 강좌 상태. 생성 시 `DRAFT`, 공개 시 `ACTIVE`, 삭제 시 `DELETED`로 전환 |

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `CourseCommandService` | 강좌 생성, 수정, 공개, 삭제 처리 |
| `CourseQueryService` | 강좌 목록, 카테고리별 목록, 상세, 강사별 목록 조회 |
| `CourseCategoryQueryService` | 강좌 카테고리 목록 조회 |

## API 목록

| Method | Path | 설명 | 권한 |
| --- | --- | --- | --- |
| `GET` | `/api/v1/courses` | 강좌 목록 조회 | 전체 |
| `GET` | `/api/v1/course-categories/{courseCategoryId}/courses` | 카테고리별 강좌 목록 조회 | 전체 |
| `GET` | `/api/v1/courses/{courseId}` | 강좌 상세 조회 | 전체 |
| `POST` | `/api/v1/courses` | 강좌 생성 | `ROLE_OPERATOR` |
| `POST` | `/api/v1/courses/thumbnails` | 강좌 썸네일 업로드 | `ROLE_OPERATOR` |
| `PUT` | `/api/v1/courses/{courseId}` | 강좌 수정 | `ROLE_OPERATOR` |
| `PATCH` | `/api/v1/courses/{courseId}/publish` | 강좌 공개 | `ROLE_OPERATOR` |
| `DELETE` | `/api/v1/courses/{courseId}` | 강좌 삭제 | `ROLE_OPERATOR` |
| `GET` | `/api/v1/course-categories` | 강좌 카테고리 목록 조회 | 전체 |
| `GET` | `/api/v1/courses/{courseId}/lecture-problem-sets` | 강좌 기준 강의 문제 세트 목록 조회 | 전체 |
| `GET` | `/api/v1/lectures/{lectureId}/lecture-problem-sets` | 강의 기준 강의 문제 세트 목록 조회 | 전체 |
| `PUT` | `/api/v1/courses/{courseId}/lecture-problem-sets` | 강좌 강의 문제 세트 연결 설정 | `ROLE_OPERATOR` |
| `GET` | `/api/v1/users/{userId}/courses` | 강사별 강좌 목록 조회 | 전체 |

## 핵심 흐름

### 강좌 생성

1. `CourseController#createCourse`가 요청을 받는다.
2. `CourseCommandService#createCourse`가 강사 권한과 활성 카테고리를 검증한다.
3. `Course.create(...)`로 `DRAFT` 상태의 강좌를 만든다.
4. `CourseRepository`를 통해 저장한다.

### 강좌 공개

1. 운영자가 `/api/v1/courses/{courseId}/publish`를 호출한다.
2. `CourseCommandService#publishCourse`가 삭제되지 않은 강좌를 조회한다.
3. `CoursePublishPolicy`가 공개 가능 조건을 검증한다.
4. `Course.publish()`로 상태를 `ACTIVE`로 변경한다.

### 강좌 삭제

1. 운영자가 `/api/v1/courses/{courseId}`에 `DELETE` 요청을 보낸다.
2. `Course.delete()`로 강좌 상태를 `DELETED`로 변경하고 `deletedAt`을 기록한다.
3. `LectureManagementPort`를 통해 해당 강좌의 강의도 함께 삭제 처리한다.

### 강좌 문제 세트 연결

1. 운영자가 `/api/v1/courses/{courseId}/lecture-problem-sets`에 연결 목록을 전달한다.
2. `LectureProblemSetCommandService`가 강좌 존재 여부를 확인한다.
3. `LectureProblemSetPolicy`가 강의, 문제 세트, 역할 등의 연결 조건을 검증한다.
4. 기존 연결이 있으면 식별자를 재사용하고, 없으면 새 연결로 저장한다.

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `lecture` | 강좌 삭제 시 하위 강의 삭제 처리, 문제세트 연결 유스케이스 제공 |
| `problems` | 문제세트 연결 요청 시 lecture 유스케이스를 통해 문제세트 존재 여부를 간접 확인 |
| `user` | 강좌 생성 시 운영자/강사 권한 검증, 강사별 강좌 조회 |
| `enrollment` | 수강 신청은 별도 도메인에서 처리하지만 강좌 ID를 기준으로 연결된다 |
| `learning` | 학습 진행률은 강좌와 강의를 기준으로 집계된다 |


