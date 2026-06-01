# Auth Domain

`auth` 도메인은 회원가입, 로그인, 로그아웃, 토큰 재발급, 이메일 인증, 이메일/닉네임 중복 확인을 담당한다. 인증 결과는 JWT와 refresh token 저장소, 쿠키 응답 흐름과 연결된다.

## 주요 역할

- 이메일 인증 코드 발송과 검증을 처리한다.
- 회원가입 요청을 검증하고 사용자 계정을 생성한다.
- 로그인 성공 시 인증 토큰을 발급하고 로그인 이력을 남긴다.
- refresh token 기반 access token 재발급을 처리한다.
- 로그아웃 시 refresh token을 무효화한다.
- 이메일과 닉네임 중복 여부를 조회한다.

## 패키지 구조

```text
auth
├── application
│   ├── command      # 로그인, 회원가입, 이메일 인증 등 명령 객체
│   ├── port         # 사용자, 메일, 토큰 관련 외부 의존 포트
│   ├── service      # 인증 유스케이스 구현체
│   └── usecase      # presentation 계층 입력 포트
├── domain
│   ├── model        # RefreshToken, LoginHistory
│   ├── repository   # 인증 저장소 인터페이스
│   └── service      # 이메일 발송 추상화
├── infrastructure
│   ├── mail         # 이메일 발송 구현
│   ├── persistence  # refresh token, 인증 코드, 로그인 이력 저장소
│   └── user         # user 도메인 연동 어댑터
└── presentation
    └── api          # REST Controller, DTO, cookie helper
```

## 주요 모델

| 모델 | 설명 |
| --- | --- |
| `RefreshToken` | 토큰 재발급에 사용되는 refresh token 상태 |
| `LoginHistory` | 사용자 로그인 기록 |

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `SignupService` | 회원가입 요청 처리 |
| `LoginService` | 로그인 검증과 토큰 발급 |
| `LogoutService` | refresh token 무효화와 로그아웃 처리 |
| `TokenReissueService` | refresh token 기반 토큰 재발급 |
| `SendVerificationEmailService` | 이메일 인증 코드 발송 |
| `VerifyEmailCodeService` | 이메일 인증 코드 검증 |
| `DuplicateCheckService` | 이메일/닉네임 중복 확인 |
| `LoginActivityQueryService` | 로그인 이력 조회 |

## API 목록

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/v1/auth/signup` | 회원가입 |
| `POST` | `/api/v1/auth/login` | 로그인 |
| `POST` | `/api/v1/auth/logout` | 로그아웃 |
| `POST` | `/api/v1/auth/reissue` | 토큰 재발급 |
| `POST` | `/api/v1/auth/email/send` | 이메일 인증 코드 발송 |
| `POST` | `/api/v1/auth/email/verify` | 이메일 인증 코드 검증 |
| `GET` | `/api/v1/auth/check/email` | 이메일 중복 확인 |
| `GET` | `/api/v1/auth/check/nickname` | 닉네임 중복 확인 |

## 핵심 흐름

### 회원가입

1. 이메일 인증 여부와 입력값 중복을 검증한다.
2. 비밀번호를 암호화하고 사용자 계정을 생성한다.
3. 사용자 역할과 인증 제공자 정보를 함께 저장한다.

### 로그인

1. 이메일과 비밀번호를 검증한다.
2. JWT access token과 refresh token을 발급한다.
3. refresh token을 저장하고 로그인 이력을 기록한다.
4. 응답 쿠키 또는 응답 본문으로 인증 정보를 전달한다.

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `user` | 계정 생성, 사용자 조회, 사용자 상태 검증 |
| `global` | JWT, Spring Security, 공통 예외 응답 사용 |

## 참고 문서

- `api-spec.md`: auth API 상세 명세
- `clean_architecture_plan.md`: auth 도메인 클린 아키텍처 전환 계획
- `convention.md`: auth 도메인 작업 컨벤션
- `handoff.md`: 인수인계 문서
