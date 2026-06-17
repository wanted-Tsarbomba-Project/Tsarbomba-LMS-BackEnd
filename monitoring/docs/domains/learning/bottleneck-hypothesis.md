# Learning 도메인 병목 가설 (Phase 1 파일럿 / 7단계 중 1단계)

> 프로세스: `../../PROCESS.md` · 컨벤션: `../../CONVENTION.md`
> 이 문서는 부하테스트 전에 "무엇이 느릴 것인가"를 코드 기준 가설로 정리하는 산출물이다.

## 도메인 유형 분류

learning 도메인은 학생 학습 진행률 기록과 관리자 학습 현황 조회를 함께 가진다. 성능개선 관점에서는 관리자 조회 API가 수강생 수, 강의 수, 문제세트 수에 따라 반복 집계 비용이 커질 가능성이 높다.

| 트랙 | 대상 API | 유형 | 6단계 처리 방식 |
|------|----------|------|------------------|
| **A. 최적화 후보** | `GET /api/v1/courses/{courseId}/users/learning-progress` | 조회/집계형, 반복 조회 | 학생별 반복 집계를 bulk 조회/집계로 바꿀 수 있는지 before/after 비교 |
| **B. 확장 후보** | `GET /api/v1/learning-progress/summary` | 전체 강좌 요약 집계 | A 트랙 개선 후 전체 요약에서도 같은 병목이 남는지 측정 |
| **C. 보조 후보** | `GET /api/v1/courses/{courseId}/lectures/learning-progress` | 강의별 완료자 count 반복 조회 | 강의 수 증가에 따른 count 반복 비용 측정 |

---

## 병목 가설표

| # | 대상 API | 병목 가설 | 근거 코드 위치 | 관찰 지표 | 성공 기준 |
|---|----------|-----------|----------------|-----------|-----------|
| 1 ⭐ | `GET /api/v1/courses/{courseId}/users/learning-progress` | **수강생별 반복 조회.** 수강생 목록을 조회한 뒤 학생마다 `buildStudentProgress()`를 호출하고, 내부에서 강의 목록, 문제세트 목록, 사용자명, 완료 강의 count, 완료 문제세트 count를 반복 조회한다. 수강생 수가 S명일 때 DB 호출 수가 선형 증가할 가능성이 높다. | `AdminLearningProgressQueryService.findStudentProgresses()` → `buildStudentProgress()` | HTTP p95, Hikari active, 커스텀 timer, Loki `durationMs` | VU 50, p95 < 500ms, 실패율 < 1% |
| 2 | `GET /api/v1/learning-progress/summary` | **전체 강좌 × 수강생 반복 집계.** 전체 active course를 순회하고 각 강좌마다 학생 진행률을 다시 계산한다. 강좌 수와 수강생 수가 함께 증가하면 병목이 커질 수 있다. | `summarizeLearningProgress()` → `findCourseProgresses()` → `buildCourseProgress()` | HTTP p95, Hikari active, 커스텀 timer | A 트랙 개선 후 비교 |
| 3 | `GET /api/v1/courses/{courseId}/lectures/learning-progress` | **강의별 완료자 count 반복.** 강의 목록을 조회한 뒤 강의마다 `countCompletedByLectureId()`를 호출한다. 강의 수가 많으면 count 쿼리가 반복된다. | `findLectureProgresses()` | HTTP p95, DB active connection | 보조 측정 |

---

## 메인 가설

`GET /api/v1/courses/{courseId}/users/learning-progress`는 수강생 수가 증가할수록 학생별 진행률 계산 과정에서 반복 조회가 발생한다.

현재 구현은 수강생 ID 목록을 가져온 뒤 각 학생마다 `buildStudentProgress(courseId, studentId)`를 호출한다. 이 메서드 안에서 같은 강좌의 강의 ID 목록과 문제세트 ID 목록을 매번 다시 조회하고, 학생별 사용자명 및 완료 count를 개별 조회한다.

따라서 수강생이 많아질수록 DB query 수와 Hikari active connection 사용량이 증가하고, HTTP p95가 선형적으로 증가할 가능성이 있다.

---

## 예상 개선 후보

1. 강좌별 `lectureIds`, `lectureProblemSetIds`는 학생 루프 밖에서 한 번만 조회한다.
2. 사용자명은 `userId IN (...)` 형태의 batch 조회로 가져온다.
3. 완료 강의 수와 완료 문제세트 수는 `GROUP BY user_id` 집계 쿼리로 한 번에 가져온다.
4. 전체 summary는 강좌별/학생별 집계를 bulk query로 분리해 중첩 반복을 줄인다.

---

## 다음 단계

- 2단계: `metrics.md` 기준으로 커스텀 Timer와 `event=learning_... durationMs=` 로그 설계
- 4단계: `monitoring/k6/scripts/learning/` 아래 baseline 스크립트 작성
- 5단계: loadtest seed로 강좌 1개, 수강생 100~300명, 강의/문제세트/진행률 데이터 생성 후 baseline 측정
