# Learning 도메인 before/after 비교 (Phase 1 / 7단계 중 6~7단계)

> 아직 baseline 측정 전이다. k6 실행 후 `monitoring/k6/results/*-summary.md` 값과 Grafana/Prometheus/Loki 관측 결과를 이 문서에 채운다.

---

## 대상

| 항목 | 내용 |
|------|------|
| 대상 API | `GET /api/v1/courses/{courseId}/users/learning-progress` |
| 병목 가설 | 수강생별 반복 조회로 인한 DB query 증가 |
| k6 스크립트 | `monitoring/k6/scripts/learning/01-student-progress-baseline.js` 예정 |
| baseline 결과 | 측정 전 |
| 개선 후 결과 | 측정 전 |

---

## 측정 조건

| 항목 | before | after |
|------|--------|-------|
| VU/stage | 예: 20s ramp-up, 40s hold VU 50, 10s ramp-down | 동일 |
| seed | 강좌 1개, 수강생 100~300명, 강의/문제세트/진행률 데이터 | 동일 |
| 성공 기준 | p95 < 500ms, 실패율 < 1% | 동일 |
| 실행 명령 | `RESULT_NAME=learning-student-progress-before` | `RESULT_NAME=learning-student-progress-after` |

---

## k6 결과

| 지표 | before | after | 변화 |
|------|--------|-------|------|
| `http_req_duration` p95 | 측정 전 | 측정 전 | - |
| `http_req_waiting` p95 | 측정 전 | 측정 전 | - |
| `http_req_failed` | 측정 전 | 측정 전 | - |
| RPS | 측정 전 | 측정 전 | - |

---

## Prometheus/Grafana 결과

| 지표 | before | after | 해석 |
|------|--------|-------|------|
| `http_server_requests` learning 평균 | 측정 전 | 측정 전 | - |
| `learning_student_progress_query_duration` 평균 | 측정 전 | 측정 전 | - |
| Hikari active connection | 측정 전 | 측정 전 | - |
| CPU/Heap | 측정 전 | 측정 전 | - |

---

## Loki 로그 확인

```logql
{job="lms"} |= "event=learning_student_progress_queried"
  | regexp "durationMs=(?P<d>[0-9]+)" | unwrap d
```

| 항목 | before | after |
|------|--------|-------|
| 대표 traceId | 측정 전 | 측정 전 |
| `studentCount` | 측정 전 | 측정 전 |
| `durationMs` 범위 | 측정 전 | 측정 전 |
| 특이 로그 | 측정 전 | 측정 전 |

---

## 결론

측정 전.

baseline에서 HTTP p95, custom timer, Hikari active가 함께 상승하면 `AdminLearningProgressQueryService`의 수강생별 반복 집계를 병목으로 확정한다. 이후 개선은 강좌별 목록 재사용, 사용자명 batch 조회, 완료 count group by 집계를 우선 적용한다.
