# Learning 도메인 메트릭/로그 설계 (Phase 1 / 7단계 중 2단계)

> 목적: HTTP 전체 시간만으로는 병목 구간을 특정하기 어렵다. learning 도메인의 의심 구간을 커스텀 Timer와 구조화 로그로 분리해서 Grafana/Prometheus/Loki에서 확인한다.

---

## 관측 대상

| 대상 | 이유 |
|------|------|
| `GET /api/v1/courses/{courseId}/users/learning-progress` | 수강생별 반복 집계 가능성이 가장 높음 |
| `AdminLearningProgressQueryService.findStudentProgresses()` | 수강생 목록 조회 후 학생별 진행률 계산 전체 구간 |
| `buildStudentProgress()` | 학생 1명 기준 반복 조회 단위 |

---

## 커스텀 Timer 후보

| 이름 | 측정 구간 | Prometheus 이름 |
|------|-----------|-----------------|
| `learning_student_progress_query_duration` | `findStudentProgresses(courseId)` 전체 | `learning_student_progress_query_duration_seconds_*` |
| `learning_student_progress_item_duration` | 학생 1명의 `buildStudentProgress()` 계산 | `learning_student_progress_item_duration_seconds_*` |
| `learning_summary_query_duration` | `summarizeLearningProgress()` 전체 | `learning_summary_query_duration_seconds_*` |
| `learning_lecture_progress_query_duration` | `findLectureProgresses(courseId)` 전체 | `learning_lecture_progress_query_duration_seconds_*` |

우선순위는 `learning_student_progress_query_duration` 하나부터 시작한다. item 단위 Timer는 cardinality를 늘리지 않도록 태그 없이 전체 분포만 본다.

---

## Loki 로그 형식

공통 규칙에 맞춰 `event=<domain>_<action> ... durationMs=<n>` 형식으로 남긴다.

```text
event=learning_student_progress_queried courseId=1 studentCount=200 durationMs=830
event=learning_student_progress_item_built courseId=1 userId=10 lectureCount=20 problemSetCount=20 durationMs=7
event=learning_summary_queried courseCount=10 durationMs=2450
event=learning_lecture_progress_queried courseId=1 lectureCount=20 durationMs=410
```

주의:
- `traceId`는 MDC가 자동으로 붙이므로 직접 label/tag로 만들지 않는다.
- `durationMs`는 로그 본문에 둔다.
- Prometheus custom metric에는 `userId`, `courseId` 같은 고카디널리티 태그를 붙이지 않는다.

---

## Grafana/Prometheus 확인 쿼리

### HTTP 평균 지연

```promql
sum by (uri) (rate(http_server_requests_seconds_sum{domain="learning"}[1m]))
/
clamp_min(sum by (uri) (rate(http_server_requests_seconds_count{domain="learning"}[1m])), 0.0001)
```

### 학생별 학습률 조회 내부 구간 평균

```promql
rate(learning_student_progress_query_duration_seconds_sum[1m])
/
clamp_min(rate(learning_student_progress_query_duration_seconds_count[1m]), 0.0001)
```

### Hikari active connection

```promql
hikaricp_connections_active
```

---

## Loki 확인 쿼리

```logql
{job="lms"} |= "event=learning_student_progress_queried"
  | regexp "durationMs=(?P<d>[0-9]+)" | unwrap d
```

특정 요청 추적:

```logql
{job="lms"} |= "event=request_completed" |= "/api/v1/courses/" |= "/users/learning-progress"
```

---

## k6 태그 제안

```javascript
params.tags = {
  type: "student-progress",
  api: "GET /courses/{courseId}/users/learning-progress"
};
```

threshold:

```javascript
thresholds: {
  http_req_failed: ["rate<0.01"],
  "http_req_duration{type:student-progress}": ["p(95)<500"],
}
```

---

## 해석 기준

| 관측 결과 | 해석 |
|-----------|------|
| HTTP p95와 `learning_student_progress_query_duration`이 같이 상승 | learning 내부 집계 구간이 병목일 가능성 높음 |
| HTTP p95는 높은데 custom timer는 낮음 | 인증, 직렬화, 네트워크, 다른 filter/interceptor 확인 |
| Hikari active가 VU 증가와 함께 높게 유지 | DB 반복 조회/connection 경합 가능성 |
| Loki durationMs가 studentCount와 함께 선형 증가 | 수강생별 반복 조회 가설 강화 |
