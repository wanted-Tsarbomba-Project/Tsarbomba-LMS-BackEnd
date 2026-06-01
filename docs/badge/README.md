# Badge Domain

`badge` 도메인은 사용자가 획득하거나 장착하는 배지와 운영자 배지 관리 기능을 담당한다. 배지 이미지 저장소와 사용자 배지 동기화 흐름도 이 도메인에 포함된다.

## 주요 역할

- 내 배지 목록을 조회한다.
- 특정 배지를 대표 배지로 장착한다.
- 사용자 배지 상태를 동기화한다.
- 운영자가 배지를 생성, 조회, 수정, 삭제한다.
- 배지 이미지 파일을 외부 저장소에 업로드한다.

## 패키지 구조

```text
badge
├── application
│   ├── command      # 배지 생성/수정 명령
│   ├── port         # 배지 조회, 이미지 저장소 포트
│   ├── query        # 배지 조회 결과
│   ├── service      # 관리자/내 배지 유스케이스 구현
│   └── usecase      # 입력 포트
├── domain
├── exception
├── infrastructure
│   ├── persistence  # 배지, 사용자 배지 저장소
│   └── storage      # GCS 배지 이미지 저장소 어댑터
└── presentation     # REST Controller, request/response DTO
```

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `AdminBadgeService` | 운영자 배지 생성, 수정, 삭제, 조회 |
| `MyBadgeService` | 내 배지 조회, 장착, 동기화 |

## API 목록

| Method | Path | 설명 | 권한 |
| --- | --- | --- | --- |
| `GET` | `/api/v1/badges/me` | 내 배지 목록 조회 | 로그인 사용자 |
| `PATCH` | `/api/v1/badges/me/{badgeId}/equip` | 대표 배지 장착 | 로그인 사용자 |
| `POST` | `/api/v1/badges/me/sync` | 내 배지 동기화 | 로그인 사용자 |
| `GET` | `/api/v1/admin/badges` | 배지 목록 조회 | 운영자 |
| `GET` | `/api/v1/admin/badges/{badgeId}` | 배지 상세 조회 | 운영자 |
| `POST` | `/api/v1/admin/badges` | 배지 생성 | 운영자 |
| `PATCH` | `/api/v1/admin/badges/{badgeId}` | 배지 수정 | 운영자 |
| `DELETE` | `/api/v1/admin/badges/{badgeId}` | 배지 삭제 | 운영자 |

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `user` | 사용자별 획득/장착 배지 관리 |
| `reward` | 보상 정책에 따라 배지 지급 조건과 연결될 수 있음 |
| `global` | 공통 응답, 예외, 파일 업로드 설정 사용 |

## 참고 문서

- `api-spec.md`: badge API 상세 명세
- `clean_architecture_plan.md`: badge 도메인 클린 아키텍처 전환 계획
- `convention.md`: badge 도메인 작업 컨벤션
- `handoff.md`: 인수인계 문서
