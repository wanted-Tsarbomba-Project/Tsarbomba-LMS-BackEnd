# 🚀 Backend Release Pull Request

> PR 제목은 반드시 `[Release] vX.Y.Z` 형식으로 작성해주세요.
>
> 예시: `[Release] v1.2.0`
>
> Release PR이 `main`에 머지되면 GitHub Actions가 PR 제목의 버전을 기준으로 태그와 GitHub Release를 생성합니다.

## 📌 릴리즈 정보

| 항목 | 내용 |
|------|------|
| **버전** | `v0.0.0 → v0.0.0` |
| **릴리즈 날짜** | YYYY-MM-DD |
| **기준 브랜치** | `develop` |
| **병합 브랜치** | `main` |
| **배포 환경** | `EC2 / Docker Compose / ECR / SSM / RDS / Redis / GCP Storage` |

---

# 📖 릴리즈 요약

> 이번 릴리즈에서 추가되거나 변경된 주요 백엔드 내용을 간략하게 작성해주세요.

-
-
-

---

# 📋 릴리즈 노트

## Auth / User

### Added

-

### Changed

-

### Fixed

-

---

## Course / Lecture / Enrollment / Learning

### Added

-

### Changed

-

### Fixed

-

---

## Problem / Submission / Ranking / Badge / Recommendation

### Added

-

### Changed

-

### Fixed

-

---

## Admin / Monitoring / Deploy

### Added

-

### Changed

-

### Fixed

-

---

# 🔌 API / DB / 환경변수 변경 사항

## API 변경

- [ ] 없음
- [ ] 있음

| 구분 | Method | URL | 변경 내용 | 프론트 영향 |
|------|--------|-----|----------|------------|
| 추가 | `GET` | `/api/v1/...` |  | 있음 / 없음 |
| 변경 | `POST` | `/api/v1/...` |  | 있음 / 없음 |
| 삭제 | `DELETE` | `/api/v1/...` |  | 있음 / 없음 |

## DB 변경

- [ ] 없음
- [ ] 있음

변경 내용:

-

## 환경변수 / Secrets 변경

- [ ] 없음
- [ ] 있음

변경 내용:

-

운영 반영 필요 여부:

- [ ] 없음
- [ ] GitHub Secrets 변경 필요
- [ ] EC2 `.env` 변경 필요
- [ ] SSM Parameter Store 변경 필요

> Secret 값은 PR에 작성하지 않고, 키 이름과 반영 위치만 작성합니다.

---

# 🧪 릴리즈 체크리스트

- [ ] `develop` 브랜치의 최신 코드가 반영되었습니다.
- [ ] `develop → main` 방향의 Release PR입니다.
- [ ] GitHub Actions CI build가 통과했습니다.
- [ ] 로컬 또는 테스트 환경에서 `./gradlew build`를 확인했습니다.
- [ ] 주요 API 및 도메인 기능 테스트를 완료했습니다.
- [ ] 인증/인가가 필요한 API의 권한 검증을 확인했습니다.
- [ ] API 변경사항이 Swagger 또는 `.ai/API.md`에 반영되었습니다.
- [ ] DB 스키마 / 시드 데이터 변경사항을 확인했습니다.
- [ ] 운영 환경변수 또는 Secrets 변경사항을 확인했습니다.
- [ ] 환경변수 / Secrets 키 추가·변경·삭제 여부를 확인했습니다.
- [ ] 변경이 있는 경우 GitHub Secrets / EC2 `.env` / SSM Parameter Store 반영 대상을 확인했습니다.
- [ ] Secret 값은 PR 본문에 노출하지 않았습니다.
- [ ] Docker / EC2 배포 설정 변경사항을 확인했습니다.
- [ ] Merge Conflict가 없습니다.
- [ ] 릴리즈 노트를 작성했습니다.
- [ ] main 머지 후 생성할 Git Tag 버전을 확인했습니다.

---

# 🏷 버전 정보

| 이전 버전 | 변경 버전 | 버전 변경 유형 |
|----------|----------|----------------|
| `v0.0.0` | `v0.0.0` | `MAJOR` / `MINOR` / `PATCH` |

### 버전 변경 사유

> 이번 버전이 MAJOR, MINOR 또는 PATCH인 이유를 작성해주세요.

예시

- **MAJOR**: 기존 API 응답 구조 변경, 인증 방식 변경 등 하위 호환이 깨지는 변경
- **MINOR**: 새로운 도메인 기능, 관리자 기능, 모니터링 기능 추가
- **PATCH**: 로그인 오류, 조회 오류, 제출 API 오류, 배포 설정 오류 수정

---

# 📦 Git Tag

> 태그는 Release PR이 `main`에 머지된 후, `main` 브랜치 기준으로만 생성합니다.

```bash
git checkout main
git pull origin main

git tag v0.0.0
git push origin v0.0.0
