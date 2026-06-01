# Global Domain

`global` 패키지는 특정 업무 도메인에 속하지 않는 공통 기반 코드를 모은다. API 응답 규격, 공통 예외, 보안 설정, JWT 처리, 로깅, JPA Auditing, 스케줄링, 정리 작업 같은 애플리케이션 공통 기능을 담당한다.

## 주요 역할

- 모든 API가 사용하는 공통 응답 형식을 제공한다.
- 도메인 예외를 표준 에러 응답으로 변환한다.
- Spring Security와 JWT 인증 필터를 구성한다.
- 요청별 MDC 로깅과 AOP 기반 비즈니스/성능 로그를 처리한다.
- JPA Auditing, Clock, Swagger, 정적 리소스, 스케줄링 설정을 제공한다.
- soft delete 이후 hard delete 정리 작업의 공통 실행 구조를 제공한다.

## 패키지 구조

```text
global
├── application
│   └── cleanup      # hard delete 실행기와 대상 포트
├── domain
│   └── common
│       ├── error    # 공통 ErrorCode, DomainException, 예외 타입
│       └── event    # DomainEvent
├── infrastructure
│   ├── cleanup      # hard delete scheduler
│   ├── config       # 보안, Swagger, JPA, Clock, Scheduling 설정
│   ├── jwt          # JWT provider, authentication filter
│   └── logging      # MDC filter, logging aspect
└── presentation
    └── api
        ├── common       # ApiResponse, ApiErrorResponse, GlobalExceptionHandler
        └── commonLegacy # legacy response DTO
```

## 주요 구성 요소

| 구성 요소 | 설명 |
| --- | --- |
| `ApiResponse` | 성공 API 응답 표준 형식 |
| `ApiErrorResponse` | 실패 API 응답 표준 형식 |
| `GlobalExceptionHandler` | 도메인/검증/인증 예외를 HTTP 응답으로 변환 |
| `SecurityConfig` | 인증/인가 필터 체인 구성 |
| `JwtTokenProvider` | JWT 생성, 검증, 파싱 |
| `JwtAuthenticationFilter` | 요청의 JWT를 인증 객체로 변환 |
| `LoggingAspect` | 비즈니스/성능 로그 AOP 처리 |
| `HardDeleteExecutor` | 도메인별 hard delete 대상 실행 |

## 다른 도메인과의 연동

| 대상 | 연동 내용 |
| --- | --- |
| 모든 presentation 계층 | 공통 API 응답과 예외 응답 사용 |
| 인증이 필요한 도메인 | JWT 인증 결과와 Spring Security 권한 사용 |
| soft delete 도메인 | hard delete scheduler와 cleanup port 사용 |
| 모든 application 계층 | 공통 도메인 예외와 logging annotation 사용 |

## 참고 문서

- `api-spec.md`: global 공통 API/응답 관련 명세
- `clean_architecture_plan.md`: global 패키지 정리 계획
- `convention.md`: global 공통 컨벤션
- `handoff.md`: 인수인계 문서
