# Code-Bomba-LMS

## 프로젝트 소개

Code-Bomba-LMS는 강좌, 강의, 문제 풀이, 제출, 학습 진행률, 운영 관리 기능을 제공하는 LMS 백엔드 프로젝트입니다.

본 프로젝트는 Spring Boot 기반으로 구현되었으며, 도메인별 책임 분리와 계층형 구조를 적용하여 유지보수성과 확장성을 고려합니다.

## 주요 기능

- 사용자 회원가입, 로그인, 로그아웃, 토큰 재발급
- 이메일 인증 및 중복 확인
- 사용자 내 정보 조회
- 강좌 생성, 수정, 조회, 삭제, 공개 처리
- 강좌 카테고리 조회
- 강사별 강좌 조회
- 강좌별 강의 등록, 수정, 조회, 삭제
- 수강 신청 및 수강 취소
- 강의 학습 진행률 저장 및 조회
- 문제 카테고리, 문제 세트, 문제, 힌트, 테스트케이스 관리
- 코드 제출 및 실행 결과 조회
- 문제 풀이 진행률 및 결과 조회
- 운영 자동화 규칙 관리
- 운영 알림 조회, 메모, 상태 변경, 삭제
- 챗봇 채팅방 및 메시지 관리

## 기술 스택

- Java 17
- Spring Boot 3.5.14
- Spring Web
- Spring Security
- Spring Data JPA
- Spring Validation
- Thymeleaf
- MySQL
- Redis
- Gradle
- JWT
- Springdoc OpenAPI Swagger
- Google Cloud Storage
- Spring Mail
- WebFlux
- H2 Test Database

## 프로젝트 구조

```text
src/main/java/com/wanted/codebombalms/
 ├─ admin/        # 운영 자동화, 운영 알림 관리
 ├─ auth/         # 인증, 회원가입, 로그인, 이메일 인증
 ├─ chatbot/      # 챗봇 채팅방 및 메시지
 ├─ course/       # 강좌, 카테고리, 강좌-문제세트 연결
 ├─ enrollment/   # 수강 신청
 ├─ global/       # 공통 설정, 예외, 응답, 보안, 로깅
 ├─ learning/     # 학습 진행률
 ├─ lecture/      # 강의
 ├─ problems/     # 문제, 문제세트, 힌트, 테스트케이스, 실행, 결과
 ├─ submission/   # 코드 제출
 └─ user/         # 사용자 정보
```

## 계층 구조

주요 도메인은 다음 계층 구조를 기준으로 구성합니다.

```text
domain-name/
 ├─ presentation/     # API 요청/응답 처리
 ├─ application/      # UseCase, Service, Command, Policy
 ├─ domain/           # Domain Model, Repository Port, Domain Exception
 └─ infrastructure/   # JPA Entity, Repository Adapter, 외부 연동
```

일부 도메인은 기존 코드와의 호환을 위해 `controller` 패키지를 함께 사용합니다.

## 주요 API 경로

### Auth

| 기능 | Method | URL |
|---|---|---|
| 회원가입 | POST | `/api/v1/auth/signup` |
| 로그인 | POST | `/api/v1/auth/login` |
| 로그아웃 | POST | `/api/v1/auth/logout` |
| 토큰 재발급 | POST | `/api/v1/auth/reissue` |
| 이메일 인증코드 발송 | POST | `/api/v1/auth/email/send` |
| 이메일 인증코드 검증 | POST | `/api/v1/auth/email/verify` |
| 이메일 중복 확인 | GET | `/api/v1/auth/check/email` |
| 닉네임 중복 확인 | GET | `/api/v1/auth/check/nickname` |

### User

| 기능 | Method | URL |
|---|---|---|
| 내 정보 조회 | GET | `/api/v1/users/me` |

### Course / Lecture

| 기능 | Method | URL |
|---|---|---|
| 강좌 목록 조회 | GET | `/api/v1/courses` |
| 강좌 상세 조회 | GET | `/api/v1/courses/{courseId}` |
| 강좌 등록 | POST | `/api/v1/courses` |
| 강좌 수정 | PUT | `/api/v1/courses/{courseId}` |
| 강좌 공개 처리 | PATCH | `/api/v1/courses/{courseId}/publish` |
| 강좌 삭제 | DELETE | `/api/v1/courses/{courseId}` |
| 강좌 카테고리 조회 | GET | `/api/v1/course-categories` |
| 강사별 강좌 조회 | GET | `/api/v1/instructors/{instructorId}/courses` |
| 강좌별 강의 조회 | GET | `/api/v1/courses/{courseId}/lectures` |
| 강의 상세 조회 | GET | `/api/v1/lectures/{lectureId}` |
| 강의 등록 | POST | `/api/v1/courses/{courseId}/lectures` |
| 강의 수정 | PUT | `/api/v1/lectures/{lectureId}` |
| 강의 삭제 | DELETE | `/api/v1/lectures/{lectureId}` |

### Enrollment / Learning

| 기능 | Method | URL |
|---|---|---|
| 수강 신청 | POST | `/api/v1/courses/{courseId}/enrollments` |
| 사용자 수강 목록 조회 | GET | `/api/v1/enrollments/{userId}` |
| 수강 취소 | DELETE | `/api/v1/enrollments/{enrollmentId}` |
| 강의 진행률 저장 | PATCH | `/api/v1/lectures/{lectureId}/progress` |
| 강의 진행률 조회 | GET | `/api/v1/lectures/{lectureId}/progress` |
| 강좌 학습 진행률 조회 | GET | `/api/v1/courses/{courseId}/learning-progress` |

### Problem / Submission

| 기능 | Method | URL |
|---|---|---|
| 문제 카테고리 조회 | GET | `/api/v1/problem-categories` |
| 문제세트 목록 조회 | GET | `/api/v1/problem-sets` |
| 문제세트 상세 조회 | GET | `/api/v1/problem-sets/{problemSetId}` |
| 문제 등록 | POST | `/api/v1/problems` |
| 문제 수정 | PUT | `/api/v1/problems/{problemSetId}` |
| 문제 삭제 | DELETE | `/api/v1/problems/{problemSetId}` |
| 문제 힌트 조회 | GET | `/api/v1/problems/{problemId}/hints` |
| 테스트케이스 등록 | POST | `/api/v1/problems/{problemId}/test-cases` |
| 테스트케이스 조회 | GET | `/api/v1/problems/{problemId}/test-cases` |
| 테스트케이스 수정 | PUT | `/api/v1/test-cases/{testCaseId}` |
| 테스트케이스 삭제 | DELETE | `/api/v1/test-cases/{testCaseId}` |
| 문제 데이터셋 등록 | POST | `/api/v1/problems/{problemId}/datasets` |
| 코드 실행 | POST | `/api/v1/code-problems/{problemId}/executions` |
| 코드 제출 | POST | `/api/v1/problems/{problemId}/submissions` |
| 제출 상세 조회 | GET | `/api/v1/code-submissions/{submissionId}` |
| 문제별 제출 목록 조회 | GET | `/api/v1/code-problems/{problemId}/submissions` |
| 문제세트 진행률 조회 | GET | `/api/v1/problem-sets/{problemSetId}/progress` |
| 문제세트 결과 조회 | GET | `/api/v1/problem-sets/{problemSetId}/result` |

### Admin / ChatBot

| 기능 | Method | URL |
|---|---|---|
| 운영 자동화 규칙 목록 조회 | GET | `/api/v1/admin/automation-rules` |
| 운영 자동화 규칙 수정 | PATCH | `/api/v1/admin/automation-rules/{automationRuleId}` |
| 운영 자동화 규칙 활성화 변경 | PATCH | `/api/v1/admin/automation-rules/{automationRuleId}/enabled` |
| 운영 알림 목록 조회 | GET | `/api/v1/admin/operation-alerts` |
| 운영 알림 상세 조회 | GET | `/api/v1/admin/operation-alerts/{operationAlertId}` |
| 운영 알림 메모 수정 | PATCH | `/api/v1/admin/operation-alerts/{operationAlertId}/memo` |
| 운영 알림 상태 변경 | PATCH | `/api/v1/admin/operation-alerts/{operationAlertId}/status` |
| 채팅방 생성 | POST | `/api/v1/chat` |
| 채팅방 목록 조회 | GET | `/api/v1/chat/list` |
| 채팅 메시지 전송 | POST | `/api/v1/chat/{roomId}/messages` |
| 채팅 메시지 조회 | GET | `/api/v1/chat/{roomId}/messages` |
| 채팅방 삭제 | DELETE | `/api/v1/chat/{roomId}` |

## API 문서

애플리케이션 실행 후 Swagger UI에서 API 문서를 확인할 수 있습니다.

```text
http://localhost:8080/swagger-ui/index.html
```

## 실행 전 준비 사항

이 프로젝트는 로컬 실행 시 DB, Redis, JWT, Mail, GCP Storage 관련 설정이 필요합니다.

로컬 환경 설정 파일은 Git에서 제외되어 있으므로 직접 생성해야 합니다.

```text
src/main/resources/application-local.yml
```

또는 팀 설정 방식에 따라 다음 파일을 사용할 수 있습니다.

```text
src/main/resources/application-local.properties
```

기본적으로는 `application-local.yml` 사용을 권장합니다.

### application-local.yml 예시

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lms
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

  sql:
    init:
      mode: never

  data:
    redis:
      host: localhost
      port: 6379

  mail:
    host: smtp.gmail.com
    port: 587
    username: your_email
    password: your_app_password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true

jwt:
  secret: your_jwt_secret
  access-expiration: 3600000
  refresh-expiration: 1209600000

fastapi:
  url: http://localhost:8000

chat:
  max-history-messages: 20

gcp:
  project-id: your_project_id
  storage:
    bucket: your_bucket_name
    dataset-prefix: problem_dataset
  credentials:
    location: file:./secrets/gcp-storage-key.json

code:
  runner:
    python-command: python
    timeout-seconds: 10
```

### application-local.properties 예시

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/lms
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false

spring.data.redis.host=localhost
spring.data.redis.port=6379

jwt.secret=your_jwt_secret
jwt.access-expiration=3600000
jwt.refresh-expiration=1209600000
```

`application-*.properties`, `application-*.yml` 형식의 파일은 모두 gitignore 처리되어 있습니다.

DB 비밀번호, JWT Secret, API Key, Mail App Password, GCP 인증 정보 등 민감한 정보는 로컬 설정 파일에만 작성하고 절대 커밋하지 않습니다.

## 실행 방법

### Mac / Linux

```bash
./gradlew bootRun
```

### Windows

```bash
.\gradlew.bat bootRun
```

## 테스트 실행

### Mac / Linux

```bash
./gradlew test
```

### Windows

```bash
.\gradlew.bat test
```

## 주요 문서

프로젝트 관련 문서는 `docs` 디렉토리에서 관리합니다.

```text
docs/
 ├─ CONVENTION.md
 ├─ ChatBot/
 ├─ PROJECT_STRUCTURE.md
 ├─ README_GUIDE.md
 └─ DOMAIN_README_TEMPLATE.md
```

| 문서 | 설명 |
|---|---|
| `docs/CONVENTION.md` | API 응답, 예외 처리, 에러코드, Swagger 컨벤션 |
| `docs/PROJECT_STRUCTURE.md` | 프로젝트 패키지 구조 통일 기준 |
| `docs/README_GUIDE.md` | README 작성 기준 |
| `docs/DOMAIN_README_TEMPLATE.md` | 도메인별 README 작성 템플릿 |
| `docs/ChatBot/` | 챗봇 도메인 관련 문서 |

## 형상관리 기준

- 기준 브랜치의 최신 변경사항을 반영한 뒤 작업합니다.
- 기능 단위로 브랜치를 생성합니다.
- PR 생성 전 실행 또는 테스트를 확인합니다.
- 다른 팀원의 기능 코드에 불필요한 변경을 하지 않습니다.
- 문서 변경이 필요한 경우 `docs` 하위 문서를 함께 수정합니다.

## gitignore 처리된 항목 요약

| 항목 | 이유 |
|---|---|
| `.idea/` | IntelliJ 개인 설정 |
| `build/` | Gradle 빌드 결과물 |
| `.gradle/` | Gradle 캐시 |
| `application-*.properties` | DB 비밀번호, API 키 등 민감 정보 |
| `application-*.yml` | DB 비밀번호, API 키 등 민감 정보 |
| `*.env`, `.env*` | 환경변수 파일 |
| `*.log`, `logs/` | 로그 파일 |
| `.DS_Store` | macOS 시스템 파일 |
| `Thumbs.db`, `Desktop.ini` | Windows 시스템 파일 |

## 주의사항

- `application.yml` 기본 파일에는 공통 설정만 작성하고 커밋합니다.
- 민감한 값은 반드시 `application-local.yml` 또는 `application-local.properties` 같은 별도 로컬 설정 파일에 분리합니다.
- `.idea/`, `.gradle/`, `build/`, `logs/`, `secrets/`는 Git에 포함하지 않습니다.
- `application-local.yml` 또는 `application-local.properties`에는 DB 비밀번호, JWT Secret, Mail App Password 등 민감 정보가 포함될 수 있으므로 절대 커밋하지 않습니다.
- Windows와 Linux는 파일명 대소문자 처리 방식이 다릅니다. Windows에서는 정상 동작하더라도 서버 또는 Linux 환경에서 오류가 발생할 수 있으므로 패키지명과 파일명을 정확히 작성합니다.
