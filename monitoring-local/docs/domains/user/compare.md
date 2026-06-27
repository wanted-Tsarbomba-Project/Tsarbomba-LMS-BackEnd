# User 도메인 — 인덱스 전후 비교 (7단계)

> 프로세스 [`../../PROCESS.md`](../../PROCESS.md). 대상: `GET /api/v1/users` (학생 목록, Admin).
> baseline → 인덱스 반영 → 재측정까지 **완주**. raw 결과: `monitoring-local/k6/results/user-student-list-{before,after}-summary.md`.

## 측정 조건 (before/after 동일)

| 항목 | 값 |
|---|---|
| 데이터 | 학생 **100만** 시드 (`db/seed/01_users_loadtest_students.sql`) |
| 부하 | ramping-vus 0→10, 약 70초 (VU 10) |
| 인증 | admin 계정 + `USER_MANAGEMENT` 권한 |
| 쿼리 | `WHERE role='STUDENT' AND deleted_at IS NULL ORDER BY created_at DESC LIMIT 20` + `COUNT(*)` |

> ⚠️ **VU는 10**이다. 50으로 주면 1M + 단건 505ms 쿼리에 Hikari 풀(기본 10)이 고갈돼 **연결 붕괴(dial timeout, 95% 실패)** 가 나 p95 측정 자체가 불가능했다. "요청이 성공하면서 느린" 구간을 재려면 VU를 낮춘다.

---

## 전후 비교표 (핵심값)

| 지표 | before (인덱스 ❌) | after (커버링 인덱스 ✅) | 개선 |
|---|---|---|---|
| k6 `http_req_duration` p95 | **807ms** | **124ms** | **6.5배** |
| k6 avg | 709ms | 100ms | 7배 |
| k6 p99 / max | 843 / 873ms | 128 / 134ms | |
| `http_req_failed` | 0% | 0% | 기능 정상(느린 것뿐) |
| 판정 (목표 p95<500ms) | **불합격** | **합격** 🎯 | |
| 목록 쿼리 EXPLAIN | `type:ALL` + `Using filesort`, 1M scan, **505ms** | `Index lookup (reverse)`, 20행, **0.05ms** | ~1만배 |
| count 쿼리 EXPLAIN | 테이블스캔, **340ms** | `Covering index lookup`, **134ms** | |

---

## 서사 — 인덱스 한 방이 아니라, 두 단계였다

### 1) baseline: 풀스캔 + filesort

`users` 엔티티에 인덱스가 없어 `WHERE role` 은 **풀스캔(`type:ALL`)**, `ORDER BY created_at` 은 **filesort**. 20개 보여주려고 100만 줄을 읽고 정렬 → 단건 505ms, 부하 p95 807ms.

```
-> Sort: created_at DESC, limit 20  (actual time=505..505)
   -> Filter: role='STUDENT' and deleted_at is null  (rows=1e+6)
      -> Table scan on users  (actual time=..341)
```

### 2) ⚠️ 1차 인덱스 `(role, created_at)` — 목록은 고쳤으나 count가 폭발

목록은 `Index lookup`으로 **0.05ms**가 됐지만, **count가 340ms → 1224ms 로 오히려 악화**. 이유: `deleted_at`이 인덱스에 없어서 count가 100만 인덱스 엔트리마다 `deleted_at IS NULL` 확인하러 **테이블을 룩업**(랜덤 I/O). LIMIT 20인 목록은 20번만 룩업해 괜찮았지만 count는 100만 번 룩업.

> 교훈: **인덱스는 가장 무거운 쿼리 기준으로 설계한다.** LIMIT 쿼리만 보고 컬럼을 빼면, 같은 테이블의 count가 새 병목이 된다.

### 3) ✅ 커버링 인덱스 `(role, deleted_at, created_at)` — 둘 다 해결

`deleted_at`을 인덱스에 포함하니 count가 **`Covering index lookup`**(테이블 접근 0) → 134ms. 목록도 그대로 0.05ms. → p95 124ms, 합격.

```
목록: Index lookup (role='STUDENT', deleted_at=NULL) (reverse)  actual 0.048ms
count: Covering index lookup (role='STUDENT', deleted_at=NULL)  actual 134ms
```

---

## 반영 (마이그레이션)

`UserJpaEntity` 에 복합 인덱스 추가 (PROCESS 7단계 — 인덱스는 `@Table(indexes=)` 로 반영):

```text
@Table(
    name = "users",
    indexes = { @Index(name = "idx_users_role_deleted_created",
                       columnList = "role, deleted_at, created_at") }
)
```

> loadtest 는 `ddl-auto: update` + 도커 볼륨 유지로 전환해, before/after 를 `CREATE INDEX` / `DROP INDEX` 로 재시드 없이 측정했다. 코드 deliverable 로는 위 `@Table(indexes=)` 를 엔티티에 둔다.

---

## 남은 한계 (정직하게)

- count 134ms 는 **100만 행 정확 카운트의 하한**(커버링이어도 1M 엔트리 스캔). 더 줄이려면 근사 카운트·카운터 캐시가 필요하나, 현재 목표(p95<500ms) 는 충분히 만족.
- 50 VU 급 부하가 상시면 Hikari 풀 사이즈 튜닝이 별도 과제(이번은 쿼리 최적화 범위).
