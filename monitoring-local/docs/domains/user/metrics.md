# User 도메인 — 심을 메트릭/로그 (7단계 §2단계)

> 프로세스 [`../../PROCESS.md`](../../PROCESS.md) · 컨벤션 [`../../CONVENTION.md`](../../CONVENTION.md)
> 메인 서사(학생 목록 인덱스 부재)만 대상. 보조 후보(login 한계측정)의 메트릭은 후속.

## 커스텀 메트릭 (Layer 3)

| Prometheus 노출명 | 종류 | 코드 등록명 | 의미 | 코드 위치 |
|---|---|---|---|---|
| `user_student_list_query_duration_seconds_{count,sum,max}` | Timer | `user_student_list_query_duration` | 학생 목록 조회 구간(list 쿼리 + count 쿼리) 시간 | `UserMetrics`(신규), [`GetStudentsService.getStudents`](../../../../src/main/java/com/wanted/codebombalms/user/application/service/GetStudentsService.java#L23) |

PromQL(평균):
```promql
rate(user_student_list_query_duration_seconds_sum[1m])
/ rate(user_student_list_query_duration_seconds_count[1m])
```

**왜 이 Timer인가:** `http_server_requests`(요청 전체)는 직렬화·네트워크까지 섞여 "쿼리만의 비용"을 못 가른다. 이 Timer는 목록 조회 구간만 분리 → 부하 중 **HTTP p95 ↑ 와 이 timer ↑ 가 동반**하면 병목 = 목록 쿼리(인덱스 부재 풀스캔/filesort) 확정. 인덱스 적용 후 이 timer가 떨어지면 최적화 효과를 직접 증명.

## 구조화 로그 (Loki)

```text
event=user_student_list_queried page=<n> size=<n> resultCount=<n> durationMs=<n>   (+ MDC traceId 자동)
```
- 위치: [`GetStudentsService.getStudents`](../../../../src/main/java/com/wanted/codebombalms/user/application/service/GetStudentsService.java#L23)
- LogQL:
```logql
{job="lms"} |= "event=user_student_list_queried"
  | regexp "durationMs=(?P<durationMs>[0-9]+)" | unwrap durationMs
```
- 보조 증거: loadtest 프로파일은 `org.hibernate.SQL DEBUG`라 **느린 요청 traceId로 실제 발행 SQL이 로그에 직접 보인다** → `EXPLAIN`과 합쳐 풀스캔/filesort 육안 확인.

## 공용 메트릭 (Layer 1·2, 추가 작업 없음)

| 출처 | 무엇 |
|---|---|
| `http_server_requests_seconds{domain="user",uri="/api/v1/users"}` | 요청 전체 p95·RPS·에러율 (DomainObservationConvention이 핸들러 패키지 `…codebombalms.user…` → `domain="user"` 자동 부착) |
| `hikaricp_connections_active` | 느린 쿼리가 커넥션 점유를 늘리는지 |
| `event=request_completed ... durationMs=` (MdcLoggingFilter) | 요청 단위 완료 로그·traceId |

> **검증 신호 정렬**: 부하 중 `http_server_requests{uri="/api/v1/users"}` p95 ↑ + `user_student_list_query_duration` ↑ + Loki `durationMs` ↑ 가 **함께** 움직이면 → 병목은 목록 쿼리. 인덱스 반영 후 셋이 같이 내려가면 → 최적화 성공.
