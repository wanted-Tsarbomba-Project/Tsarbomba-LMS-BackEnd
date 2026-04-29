# Code-Bomba-LMS

## 로컬 환경 설정

이 프로젝트를 클론한 후 아래 파일들은 **git에서 제외**되어 있으므로 직접 생성해야 합니다.

---

### application-local.properties

`Code-Bomba-LMS/src/main/resources/` 아래에 생성하세요.

```properties
# DB 설정 예시
spring.datasource.url=jdbc:mysql://localhost:3306/lms
spring.datasource.username=your_username
spring.datasource.password=your_password

# 기타 로컬 환경 설정
```

> `application-*.properties` / `application-*.yml` 형식의 파일은 모두 gitignore 처리되어 있습니다.
> DB 비밀번호, API 키 등 민감한 정보는 이 파일에만 작성하고 절대 커밋하지 마세요.

---

## gitignore 처리된 항목 요약

| 항목 | 이유 |
|---|---|
| `.idea/` | IntelliJ 개인 설정 (팀원마다 다름) |
| `build/` | Gradle 빌드 결과물 |
| `.gradle/` | Gradle 캐시 |
| `application-*.properties` | DB 비밀번호, API 키 등 시크릿 |
| `application-*.yml` | 동일 |
| `*.env`, `.env*` | 환경변수 파일 |
| `*.log`, `logs/` | 로그 파일 |
| `.DS_Store` | macOS 시스템 파일 |
| `Thumbs.db`, `Desktop.ini` | Windows 시스템 파일 |

맥유저가 저밖에 없어서 슬프네요...

---

## 주의사항

- `application.properties`(기본 파일)는 공통 설정만 작성하고 커밋합니다.
- 민감한 값은 반드시 `application-local.properties` 같은 별도 파일에 분리하세요.

### Windows 사용자 주의사항

- **파일명 대소문자**: Windows는 대소문자를 구분하지 않아 로컬에서는 정상이지만 서버(Linux)에서 오류가 날 수 있습니다. 파일명은 항상 정확하게 작성하세요.
