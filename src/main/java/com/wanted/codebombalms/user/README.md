# User Domain

`user` 도메인은 사용자 기본 정보, 역할, 계정 잠금 상태, 내 프로필, 관리자용 학생 조회 기능을 담당한다. 인증 도메인에서 생성된 계정을 LMS 운영과 학습 조회에 필요한 형태로 제공한다.

## 주요 역할

- 내 프로필을 조회한다.
- 사용자 목록을 조회한다.
- 관리자 화면에서 학생 상세 정보를 조회한다.
- 학생별 문제 제출 이력을 조회한다.
- 사용자 계정을 잠금/해제한다.
- 다른 도메인이 사용자 정보를 조회할 수 있는 포트를 제공한다.

## 패키지 구조

```text
user
├── application
│   ├── port         # 학생 제출 조회 등 외부 조회 포트
│   ├── service      # 사용자 조회/잠금 서비스
│   └── usecase      # 입력 포트
├── domain
│   ├── model        # User, UserRole, AuthProvider
│   └── repository   # 사용자 저장소 인터페이스
├── infrastructure
│   └── persistence  # JPA 엔티티, repository adapter, projection
└── presentation
    └── api          # REST Controller, request/response DTO
```

## 주요 모델

| 모델 | 설명 |
| --- | --- |
| `User` | 사용자 기본 정보와 상태 |
| `UserRole` | 사용자 권한 역할 |
| `AuthProvider` | 가입/인증 제공자 |

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `GetMyProfileService` | 로그인 사용자 프로필 조회 |
| `GetStudentsService` | 사용자/학생 목록 조회 |
| `GetStudentDetailService` | 관리자용 학생 상세 조회 |
| `GetStudentProblemSubmissionsService` | 학생별 문제 제출 조회 |
| `ChangeStudentLockService` | 사용자 잠금 상태 변경 |
| `UserOperationQueryService` | 운영 기능에서 필요한 사용자 조회 |

## API 목록

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/api/v1/users/me` | 내 프로필 조회 |
| `GET` | `/api/v1/users` | 사용자 목록 조회 |
| `PATCH` | `/api/v1/users/{userId}/lock` | 사용자 잠금 상태 변경 |
| `GET` | `/api/v1/admin/users/{userId}` | 관리자용 사용자 상세 조회 |
| `GET` | `/api/v1/admin/students/{userId}/problems` | 학생 문제 제출 목록 조회 |

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `auth` | 회원가입, 로그인, 인증 대상 사용자 저장/조회 |
| `course` | 강좌 생성자와 강사별 강좌 조회 |
| `enrollment` | 수강 신청 사용자 검증 |
| `learning` | 사용자별 학습 진행률 집계 |
| `submission` | 학생별 제출 이력 조회 |
| `reward` | 사용자별 포인트 저장 기준 |

