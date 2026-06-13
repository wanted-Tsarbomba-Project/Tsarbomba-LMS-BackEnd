# Admin Master Permission 기획서

> 목적: 기존 admin 페이지 구조를 최대한 유지하면서, `MASTER`가 여러 `ADMIN` 계정의 세부 권한을 관리할 수 있도록 한다.

---

## 1. 배경

현재 `ADMIN` 계정은 유저 관리와 운영 규칙 관리를 모두 수행한다. 앞으로는 admin 계정을 여러 개 운영하면서 계정별로 담당 범위를 다르게 가져가고 싶다.

다만 프론트엔드에서 기존 admin 화면을 권한별로 세세하게 분리하면 수정 범위가 커지므로, 프론트는 `MASTER` 전용 admin 계정 관리 메뉴를 추가하고 세부 권한 검증은 백엔드에서 처리한다.

---

## 2. 목표

| 항목 | 내용 |
|------|------|
| 최고 관리자 추가 | `MASTER` role을 추가한다. |
| admin 권한 분리 | `ADMIN` 계정별로 유저 관리/규칙 관리 권한을 부여한다. |
| 프론트 수정 최소화 | 기존 admin 메뉴는 유지하고, `MASTER` 전용 admin 계정 관리 메뉴만 추가 노출한다. |
| 백엔드 보안 강화 | 세부 권한이 없는 API 접근은 백엔드에서 403 예외로 차단한다. |
| 확장 가능성 | 추후 권한 종류를 늘릴 수 있도록 별도 permission 엔티티로 관리한다. |

---

## 3. Role 정책

| Role | 설명 | 접근 정책 |
|------|------|----------|
| `STUDENT` | 일반 학습자 | admin 기능 접근 불가 |
| `OPERATOR` | 강좌/문제 운영자 | 기존 operator 정책 유지 |
| `ADMIN` | 일반 관리자 | 부여받은 admin permission 기준으로 API 접근 |
| `MASTER` | 최고 관리자 | 기존 admin 기능 전체 접근 + admin 계정/권한 관리 |

---

## 4. Permission 정책

| Permission | 설명 | 대상 API |
|------------|------|----------|
| `USER_MANAGEMENT` | 유저 관리 권한 | 학생 목록/상세/제출 내역/잠금 처리 |
| `RULE_MANAGEMENT` | 운영 규칙 관리 권한 | 운영 알림, 자동화 규칙 조회/수정 |

권한 검증 원칙:

| 조건 | 처리 |
|------|------|
| 요청자가 `MASTER` | 모든 admin permission 검증 통과 |
| 요청자가 `ADMIN` | API가 요구하는 permission 보유 시 통과 |
| 요청자가 `ADMIN`이지만 permission 없음 | 403 예외 |
| 요청자가 `STUDENT` | 403 예외 |
| 요청자가 인증되지 않음 | 401 예외 |

---

## 5. 프론트엔드 기획

프론트는 세부 permission 기준으로 기존 메뉴를 숨기지 않는다. 기존 admin 페이지 접근은 유지하고, 권한이 없는 API를 호출하면 백엔드의 403 메시지를 공통 에러 처리로 보여준다.

`MASTER` 전용 admin 계정 관리 화면에서는 admin 목록을 조회하고, 각 계정의 `USER_MANAGEMENT`, `RULE_MANAGEMENT` 권한을 칼럼 단위로 보여준다. 각 칼럼에는 현재 권한 상태와 권한 부여/회수 버튼을 배치한다.

### 프론트 작업 범위

| 작업 | 내용 |
|------|------|
| role 확인 | 로그인 응답 또는 내 정보 API의 `role` 값으로 `MASTER` 여부 확인 |
| 사이드바 분기 | `role === "MASTER"`일 때만 admin 계정 관리 메뉴 노출 |
| 403 공통 처리 | 백엔드 권한 예외 메시지를 토스트/모달/알림으로 표시 |
| master 페이지 추가 | admin 계정 목록, `user` 권한 칼럼, `rule` 권한 칼럼, 권한 부여/회수 버튼 추가 |
| 기존 admin 페이지 유지 | 유저 관리/규칙 관리 메뉴는 세부 permission 기준으로 숨기지 않음 |

### 프론트 권장 화면

| 화면 | 노출 조건 | 기능 |
|------|----------|------|
| 기존 유저 관리 | 기존 admin 사이드바 정책 유지 | 권한 없으면 API 403 메시지 표시 |
| 기존 운영 알림 | 기존 admin 사이드바 정책 유지 | 권한 없으면 API 403 메시지 표시 |
| 기존 자동화 규칙 | 기존 admin 사이드바 정책 유지 | 권한 없으면 API 403 메시지 표시 |
| Admin 계정 관리 | `MASTER`만 노출 | admin 목록 조회 |
| Admin 권한 관리 | `MASTER`만 노출 | `user`/`rule` 칼럼별 권한 부여/회수 |

### Admin 계정 관리 테이블

| 칼럼 | 표시 내용 | 액션 |
|------|----------|------|
| admin 정보 | 이메일, 이름, 닉네임, 잠금 여부, 생성일 | 없음 |
| user 권한 | `USER_MANAGEMENT` 보유 여부 | 권한 부여 / 권한 회수 |
| rule 권한 | `RULE_MANAGEMENT` 보유 여부 | 권한 부여 / 권한 회수 |

권한 버튼은 현재 상태 기준으로 반대 액션을 노출한다.

| 현재 상태 | 버튼 |
|-----------|------|
| 권한 있음 | 권한 회수 |
| 권한 없음 | 권한 부여 |

---

## 6. 백엔드 기획

### 수정 대상

| 영역 | 작업 |
|------|------|
| user domain | `UserRole`에 `MASTER` 추가 |
| admin domain | `AdminPermissionType` enum 추가 |
| admin domain | `AdminPermission` 엔티티 추가 |
| admin persistence | admin permission repository/adapter 추가 |
| admin application | admin 권한 조회/검증/수정 service 추가 |
| security | `MASTER` 전체 통과, `ADMIN` permission 검증 정책 추가 |
| API | master 전용 admin 계정 목록 조회 API 추가 |
| API | master 전용 admin 단일 권한 부여/회수 API 추가 |
| 기존 API | 유저 관리 API에 `USER_MANAGEMENT` 검증 적용 |
| 기존 API | 운영 알림/자동화 규칙 API에 `RULE_MANAGEMENT` 검증 적용 |

### 권장 패키지

| 패키지 | 역할 |
|--------|------|
| `com.wanted.codebombalms.admin.permission.domain.model` | permission enum/model |
| `com.wanted.codebombalms.admin.permission.domain.repository` | permission repository port |
| `com.wanted.codebombalms.admin.permission.application.service` | 권한 조회/검증/수정 service |
| `com.wanted.codebombalms.admin.permission.application.usecase` | usecase interface |
| `com.wanted.codebombalms.admin.permission.infrastructure.persistence` | JPA entity/repository/adapter |
| `com.wanted.codebombalms.admin.permission.presentation.api` | controller/response code/message |
| `com.wanted.codebombalms.admin.permission.presentation.api.request` | request DTO |
| `com.wanted.codebombalms.admin.permission.presentation.api.response` | response DTO |

---

## 7. DB 설계

### users

기존 `users.role` enum에 `MASTER` 값을 추가한다.

| 컬럼 | 변경 내용 |
|------|----------|
| `role` | `STUDENT`, `OPERATOR`, `ADMIN`, `MASTER` 허용 |

### admin_permissions

| 컬럼 | 타입 예시 | 설명 |
|------|----------|------|
| `admin_permission_id` | BIGINT | PK |
| `admin_user_id` | BIGINT | 권한을 받는 admin user id |
| `permission_type` | VARCHAR(50) | `USER_MANAGEMENT`, `RULE_MANAGEMENT` |
| `granted_by` | BIGINT | 권한을 부여한 master user id |
| `created_at` | DATETIME | 생성일 |
| `updated_at` | DATETIME | 수정일 |

제약 조건:

| 제약 | 내용 |
|------|------|
| Unique | `(admin_user_id, permission_type)` 중복 방지 |
| FK | `admin_user_id -> users.user_id` |
| FK | `granted_by -> users.user_id` |
| Service validation | 권한 대상은 `ADMIN` role만 허용 |
| Service validation | 권한 부여자는 `MASTER` role만 허용 |

---

## 8. API 설계 요약

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| `GET` | `/api/v1/admin/accounts` | admin 계정 목록 조회 | `MASTER` |
| `PATCH` | `/api/v1/admin/accounts/{adminUserId}/permissions` | admin 계정 단일 권한 부여/회수 | `MASTER` |

상세 명세는 `.ai/API.md`를 기준으로 관리한다.

### `/api/v1/admin/me/permissions` 판단

현재 MVP에서는 별도 구현하지 않는다.

| 판단 항목 | 내용 |
|-----------|------|
| MASTER 메뉴 노출 | 로그인 응답 또는 `/api/v1/users/me`의 `role`로 충분 |
| 일반 ADMIN 세부 권한 | 권한 없는 API 접근 시 백엔드 403 응답으로 안내 |
| 추후 필요 조건 | permission 기준 메뉴 숨김, 내 권한 배지 표시, 권한 변경 즉시 화면 동기화가 필요할 때 추가 |

---

## 9. 예외 정책

| HTTP | code | message | 상황 |
|------|------|---------|------|
| 403 | `ADM-AUTH-001` | 관리자 권한이 없습니다. | admin/master가 아닌 계정 접근 |
| 403 | `ADM-AUTH-002` | 유저 관리 권한이 없습니다. | `USER_MANAGEMENT` 없음 |
| 403 | `ADM-AUTH-003` | 운영 규칙 관리 권한이 없습니다. | `RULE_MANAGEMENT` 없음 |
| 403 | `ADM-AUTH-004` | 최고 관리자만 접근할 수 있습니다. | master 전용 API 접근 |
| 400 | `ADM-AUTH-005` | 관리자 권한 수정 요청이 올바르지 않습니다. | request validation 실패 |
| 404 | `ADM-AUTH-006` | 관리자 계정을 찾을 수 없습니다. | 대상 admin 없음 |
| 409 | `ADM-AUTH-007` | 관리자 권한 상태가 올바르지 않습니다. | ADMIN이 아닌 계정에 권한 부여 시도 |

---

## 10. 구현 순서

| 순서 | 작업 |
|------|------|
| 1 | `UserRole.MASTER` 추가 |
| 2 | `AdminPermissionType`, `AdminPermission` 도메인 추가 |
| 3 | admin permission JPA entity/repository/adapter 추가 |
| 4 | admin 권한 조회/검증 service 추가 |
| 5 | master 전용 admin 계정 목록 조회 API 추가 |
| 6 | master 전용 admin 단일 권한 부여/회수 API 추가 |
| 7 | 유저 관리 API에 `USER_MANAGEMENT` 검증 적용 |
| 8 | 운영 알림/자동화 규칙 API에 `RULE_MANAGEMENT` 검증 적용 |
| 9 | 기존 admin 계정 권한 마이그레이션 |
| 10 | 프론트 사이드바 master 메뉴와 403 공통 처리 연동 |

---

## 11. 결정 사항

| 항목 | 결정 |
|------|------|
| 프론트 세부 권한 메뉴 분기 | 하지 않음 |
| master 전용 메뉴 분기 | 프론트에서 `role === "MASTER"` 기준으로 처리 |
| 세부 권한 검증 | 백엔드에서 처리 |
| 기존 admin 페이지 | 유지 |
| admin permission 저장 위치 | admin 패키지의 별도 엔티티 |
| JWT에 permission 포함 여부 | 포함하지 않는 방향 권장 |
| 내 권한 조회 API | MVP에서는 제외하고 필요 시 재검토 |
| 권한 수정 방식 | 단일 권한 부여/회수 API로 처리 |
| 권한 수정 멱등성 | 이미 부여/회수된 상태여도 성공 처리 후 현재 상태 반환 |
