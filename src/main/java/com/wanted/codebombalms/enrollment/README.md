# Enrollment Domain

`enrollment` 도메인은 사용자의 강좌 수강 신청과 수강 목록 관리를 담당한다. 강좌와 사용자 사이의 등록 상태를 저장하고, 관리자/사용자 관점의 조회 API를 제공한다.

## 주요 역할

- 사용자가 강좌를 수강 신청한다.
- 사용자별 수강 신청 목록을 조회한다.
- 전체 수강 신청 목록을 조회한다.
- 사용자의 특정 수강 신청을 취소하거나 삭제한다.

## 패키지 구조

```text
enrollment
├── application
│   ├── command      # 수강 신청/삭제 명령
│   ├── port         # course, user 도메인 연동 포트
│   ├── service      # 수강 신청 명령/조회 서비스
│   └── usecase      # 입력 포트
├── domain
│   ├── model        # Enrollment, EnrollmentStatus
│   └── repository   # 수강 신청 저장소 인터페이스
├── infrastructure
│   ├── course       # course 도메인 조회 어댑터
│   ├── persistence  # JPA 저장소 구현
│   └── user         # user 도메인 조회 어댑터
└── presentation
    └── api          # REST Controller, response DTO
```

## 주요 모델

| 모델 | 설명 |
| --- | --- |
| `Enrollment` | 사용자와 강좌의 수강 신청 관계 |
| `EnrollmentStatus` | 수강 신청 상태 |

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `EnrollmentCommandService` | 수강 신청 생성과 삭제 처리 |
| `EnrollmentQueryService` | 사용자별/전체 수강 신청 조회 |

## API 목록

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/v1/courses/{courseId}/enrollments` | 강좌 수강 신청 |
| `GET` | `/api/v1/users/{userId}/enrollments` | 사용자별 수강 신청 목록 조회 |
| `GET` | `/api/v1/enrollments` | 전체 수강 신청 목록 조회 |
| `DELETE` | `/api/v1/users/{userId}/enrollments/{enrollmentId}` | 수강 신청 삭제 |

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `course` | 신청 대상 강좌 존재 여부와 상태 확인 |
| `user` | 신청 사용자 존재 여부 확인 |
| `learning` | 수강 신청 이후 학습 진행률 조회 기준으로 사용 |

