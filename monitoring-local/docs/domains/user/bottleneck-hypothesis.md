# User 도메인 — 병목 가설 (7단계 §1단계)

> 프로세스: [`../../PROCESS.md`](../../PROCESS.md) · 컨벤션: [`../../CONVENTION.md`](../../CONVENTION.md)
> 이 문서는 **부하테스트 전에 "무엇이 왜 느릴 것인가"를 가설로 박는** 산출물이다. baseline·전후비교로 검증한다.

## 도메인 유형 분류

User 도메인 API를 PROCESS 4유형으로 분류해 측정 대상을 좁힌다.

| 대상 API | 유형 | 자체 병목 성격 | 측정 가치 |
|----------|------|----------------|-----------|
| **`GET /api/v1/users`** (학생 목록, Admin) ★ | 조회/집계형 | `role` 필터 + `created_at` 정렬에 **인덱스 부재** → 풀스캔 + filesort | 메인 (전후 비교 주인공) |
| `GET /api/v1/admin/students/{id}/problems` (제출 내역) | 조회/집계형 | 다중 join + 정렬 | 보조 — submission·problems **타 도메인 테이블 의존**이라 후순위 |
| `POST /api/v1/auth/login` | 쓰기/CPU | BCrypt(의도적 고비용) + `@Transactional` 안 3-write | 보조 — 최적화 아닌 **한계 측정**용 |
| `GET /api/v1/users/me`, 중복확인 | 단순 CRUD | PK/unique 단건 | 병목 없음 (억지 최적화 금지) |

> **메인은 학생 목록 하나.** 최적화 무기(인덱스)·반영·after 측정이 전부 `users` 테이블(이 도메인 소유) 안에서 끝나 7단계를 자체 완결로 완주할 수 있다.

---

## 병목 가설표

| # | 대상 API | 병목 가설 | 근거 (코드 위치) | 관찰 지표 | 성공 기준 |
|---|----------|-----------|------------------|-----------|-----------|
| 1 ★ | `GET /api/v1/users` | **인덱스 부재 → 풀스캔 + filesort.** 쿼리는 `WHERE role='STUDENT' AND deleted_at IS NULL ORDER BY created_at DESC LIMIT ?,?` 인데 `users` 엔티티에 `@Table(indexes=…)`가 전혀 없다. 학생 수↑일수록 전체 스캔 + 정렬 비용이 선형 악화 | [`UserJpaEntity`](../../../../src/main/java/com/wanted/codebombalms/user/infrastructure/persistence/UserJpaEntity.java#L17) (인덱스 선언 없음, `@SQLRestriction("deleted_at IS NULL")`) → [`findAllByRoleOrderByCreatedAtDesc`](../../../../src/main/java/com/wanted/codebombalms/user/infrastructure/persistence/UserRepositoryAdapter.java#L64) → [`GetStudentsService.getStudents`](../../../../src/main/java/com/wanted/codebombalms/user/application/service/GetStudentsService.java#L23) | `http_server_requests_seconds{domain="user",uri="/api/v1/users"}` p95, 커스텀 `user_student_list_query_duration`, `EXPLAIN`의 `type`/`Extra(Using filesort)`/rows, Hikari active | `http_req_duration{type:student_list}` p95 < 500ms, 실패율 < 1% (VU 50, 5분, 학생 다수 시드) |
| 2 | `GET /api/v1/admin/students/{id}/problems` | **다중 join + 정렬.** `submission`·`problem`·`problem_set`·`point_history` 4테이블 join 후 4중 정렬. 제출 많을수록 비용 증가 | [`StudentProblemSubmissionQueryAdapter.findByCondition`](../../../../src/main/java/com/wanted/codebombalms/user/infrastructure/persistence/StudentProblemSubmissionQueryAdapter.java#L21) | 위와 동일 패턴, 응답 크기 | 보조 후보 — 타 도메인 테이블 의존이라 v1은 가설만 |
| 3 | `POST /api/v1/auth/login` | **CPU + 쓰기 트랜잭션 한계.** `passwordEncoder.matches`(BCrypt)는 의도적으로 무겁다. `@Transactional` 안에서 RT삭제·RT저장·이력저장 3-write를 BCrypt와 함께 한 커넥션이 점유 | [`LoginService.login`](../../../../src/main/java/com/wanted/codebombalms/auth/application/service/LoginService.java#L34) (`@Transactional`, BCrypt + 3-write) | async 아님, Hikari active, CPU usage, 동시 로그인 시 p95/타임아웃 | **합격/불합격 아님.** "몇 동시 로그인까지 5xx/풀고갈 없이 버티나"가 산출물 |

★ = 메인 서사 (전후 비교의 주인공).

---

## 최적화 후보 (6→7단계에서 검증)

N+1이 아니라 **인덱스 부재**다. 인덱스로 직접 풀린다(조회형 정석 플레이북).

1. **복합 인덱스** — `users(role, deleted_at, created_at)`. `role` 필터 + `deleted_at IS NULL` + `created_at` 정렬을 한 인덱스로 커버 → 풀스캔·filesort 제거.
2. (대안) `users(role, created_at)` 단순 복합 — `deleted_at`을 인덱스에서 빼고 필터만. 카디널리티 낮은 `deleted_at`을 선두에서 제외.

> **반영 방식**: Flyway 없고 `ddl-auto: create`라 수동 `CREATE INDEX`는 재부팅 시 날아간다. 인덱스는 [`UserJpaEntity`](../../../../src/main/java/com/wanted/codebombalms/user/infrastructure/persistence/UserJpaEntity.java#L17)의 `@Table(indexes = @Index(...))` 어노테이션으로 반영한다 (PROCESS 7단계 "마이그레이션" 규칙).
>
> 기대: baseline에서 학생 수↑일수록 p95 선형 악화 → 인덱스 후 평탄화. 전후 비교표(`EXPLAIN` type/rows/Extra + p95 + custom query duration)로 증명.

---

## 다음 단계

- **2단계**: 커스텀 메트릭 심기 — `user_student_list_query_duration` Timer, `event=user_student_list_queried` 로그. → [`metrics.md`](metrics.md)
- **4·5단계**: `monitoring-local/k6/scripts/user/` 시나리오 + baseline (학생 다수 시드 선행, `db/seed/`)
- **7단계**: 인덱스 반영 → 재측정 → [`compare.md`](compare.md)
