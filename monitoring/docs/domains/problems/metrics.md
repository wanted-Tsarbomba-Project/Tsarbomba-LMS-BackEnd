# Problems 도메인 메트릭 / 로그 / 부하 테스트 설계

> 프로세스: `monitoring/docs/PROCESS.md` 2~5단계 산출물  
> 병목 가설: `monitoring/docs/domains/problems/bottleneck-hypothesis.md`

이 문서는 Problems 관련 병목 후보 1, 2순위를 실제로 측정하기 위한 메트릭, 로그, 성공 기준, k6 시나리오, 시드 및 baseline 실행 방법을 정리한다.

---

## 1단계 요약 - 측정 대상 API

| 우선순위 | API | 유형 | 병목 가설 |
| --- | --- | --- | --- |
| 1 | `POST /api/v1/problems/{problemId}/submissions` | 쓰기 + 외부 연동 | 사용자 코드 실행, 테스트케이스 채점, 제출 결과 저장, 포인트 이벤트 발행이 한 흐름에 이어져 p95가 커질 수 있다. |
| 2 | `GET /api/v1/problem-sets/{problemSetId}` | 조회 / 집계 | 문제세트 진입 시 문제 목록, 진행 상태, 최신 제출, 시작 코드를 함께 구성하므로 조회 조합 비용이 커질 수 있다. |

---

## 2단계 - Custom Metrics

| metric name | type | 대상 API | 의미 | 코드 위치 |
| --- | --- | --- | --- | --- |
| `submission_grading_duration_seconds` | Timer | 제출/채점 | 제출 요청에서 채점과 결과 저장까지 걸린 시간 | `submission.infrastructure.metrics.SubmissionMetrics.recordGrading()` |
| `code_execution_runner_duration_seconds` | Timer | 코드 실행 / 제출 | Python Runner 호출과 응답 수신에 걸린 시간 | `problems.execution.infrastructure.metrics.CodeExecutionMetrics.recordRunnerExecution()` |
| `problem_set_entry_duration_seconds` | Timer | 문제세트 진입 | 문제세트 진입 화면 데이터 구성에 걸린 시간 | `problems.set.infrastructure.metrics.ProblemSetMetrics.recordEntry()` |
| `submission_failed_total` | Counter | 제출/채점 | 제출 또는 채점 실패 횟수 | `submission.infrastructure.metrics.SubmissionMetrics.incrementFailure()` |

> Micrometer Timer는 Prometheus에서 `_seconds_count`, `_seconds_sum`, `_seconds_max` 형태로 노출된다.

---

## 2단계 - Structured Logs

| event | 대상 API | 남길 값 | 목적 |
| --- | --- | --- | --- |
| `event=submission_completed` | 제출/채점 | `userId`, `problemId`, `submissionId`, `isCorrect`, `passedTestCount`, `totalTestCount`, `durationMs` | 정상 제출의 전체 처리 시간 확인 |
| `event=submission_failed` | 제출/채점 | `userId`, `problemId`, `exceptionType`, `durationMs` | 제출/채점 실패 원인 분류 |
| `event=code_execution_runner_completed` | Runner 호출 | `success`, `executionTimeMs`, `durationMs` | 외부 Runner 지연 여부 확인 |
| `event=code_execution_runner_failed` | Runner 호출 | `endpoint`, `exceptionType`, `durationMs` | Runner 호출 실패 원인 확인 |
| `event=problem_set_entered` | 문제세트 진입 | `userId`, `problemSetId`, `problemCount`, `solvedProblemCount`, `durationMs` | 문제세트 화면 구성 시간 확인 |
| `event=problem_set_entry_failed` | 문제세트 진입 | `userId`, `problemSetId`, `exceptionType`, `durationMs` | 문제세트 진입 실패 원인 확인 |

로그는 `event=<domain>_<verb> ... durationMs=<n>` 형식을 따른다.

---

## 3단계 - 성공 기준

반드시 트래픽 조건과 측정 기간을 함께 적는다. `평균 500ms 이하`처럼 평균만 보는 기준은 사용하지 않는다.

### 3-1. 문제세트 진입 API

```http
GET /api/v1/problem-sets/{problemSetId}
```

| 항목 | 기준 |
| --- | --- |
| 트래픽 조건 | VU 50, ramping 0 -> 50, 약 70초 |
| API 유형 | 조회 / 집계 |
| 성공 기준 | `http_req_duration{type:entry}` p95 < 800ms |
| tail latency 기준 | `http_req_duration{type:entry}` p99 < 2s |
| 실패율 | `http_req_failed < 1%` |
| check 성공률 | 99% 이상 |
| 함께 볼 메트릭 | `problem_set_entry_duration_seconds` |

실패 해석:

| 실패 상황 | 의심 지점 |
| --- | --- |
| p95가 800ms 초과 | 문제세트 조회 조합, 진행 상태 조회, startCode 구성 |
| p99가 2초 초과 | 일부 요청의 DB 조회 지연 또는 커넥션 대기 |
| HTTP duration과 `problem_set_entry_duration_seconds`가 함께 증가 | application 내부 조회 조합 병목 |
| HTTP만 높고 custom metric은 낮음 | 인증, 필터, 네트워크, 공통 계층 의심 |

### 3-2. 코드 제출/채점 API

```http
POST /api/v1/problems/{problemId}/submissions
```

| 항목 | 기준 |
| --- | --- |
| 트래픽 조건 | VU 50, ramping 0 -> 50, 약 70초 |
| API 유형 | 쓰기 + 외부 연동 |
| 성공 기준 | `http_req_duration{type:submit}` p95 < 2s |
| tail latency 기준 | `http_req_duration{type:submit}` p99 < 5s |
| 실패율 | `http_req_failed < 1%` |
| check 성공률 | 99% 이상 |
| 함께 볼 메트릭 | `submission_grading_duration_seconds`, `code_execution_runner_duration_seconds`, `submission_failed_total` |

실패 해석:

| 실패 상황 | 의심 지점 |
| --- | --- |
| p95가 2초 초과 | Runner, 테스트케이스 채점, DB 저장 중 병목 가능 |
| p99가 5초 초과 | timeout 근처 요청 또는 외부 Runner 지연 |
| Runner duration만 높음 | Python Runner / Cloud Run / 네트워크 병목 |
| Runner는 낮고 submission grading만 높음 | 테스트케이스 조회, 제출 저장, 진행 상태 갱신 병목 |
| `submission_failed_total` 증가 | 제출/채점 예외 증가 |

---

## 4단계 - k6 시나리오

Problems 도메인은 병목 유형이 다르므로 k6 시나리오를 2개로 분리한다.

| 파일 | 대상 | 설명 |
| --- | --- | --- |
| `monitoring/k6/scripts/problems/01-problem-set-entry-baseline.js` | 문제세트 진입 | 조회/집계형 API baseline |
| `monitoring/k6/scripts/problems/02-submission-baseline.js` | 코드 제출/채점 | 쓰기 + 외부 Runner 연동 API baseline |

### 4-1. 문제세트 진입 시나리오

주요 조건:

| 항목 | 값 |
| --- | --- |
| 기본 문제세트 ID | `4001` |
| 환경변수 | `PROBLEM_SET_ID`로 변경 가능 |
| k6 tag | `type=entry` |
| threshold | p95 < 800ms, p99 < 2s, failed < 1% |

실행 대상 파일:

```text
monitoring/k6/scripts/problems/01-problem-set-entry-baseline.js
```

### 4-2. 코드 제출/채점 시나리오

주요 조건:

| 항목 | 값 |
| --- | --- |
| 기본 문제 ID | `5001` |
| 기본 제출 코드 | `result = None` |
| 환경변수 | `PROBLEM_ID`, `SUBMISSION_CODE`로 변경 가능 |
| k6 tag | `type=submit` |
| threshold | p95 < 2s, p99 < 5s, failed < 1% |

기본 코드를 오답 코드로 둔 이유:

| 이유 | 설명 |
| --- | --- |
| 반복 제출 안정성 | 정답 코드를 반복 제출하면 이미 푼 문제 정책에 막힐 수 있다. |
| 병목 측정 가능 | 오답이어도 Runner 실행, 테스트케이스 채점, 제출 저장 흐름은 실행된다. |
| 포인트 중복 방지 | 정답 이벤트와 포인트 지급을 반복 발생시키지 않는다. |

실행 대상 파일:

```text
monitoring/k6/scripts/problems/02-submission-baseline.js
```

---

## 5단계 - 시드 + baseline

### 5-1. 시드가 필요한 이유

loadtest DB는 테스트 환경에서 비어 있거나 데이터가 적을 수 있다. 데이터가 너무 적으면 조회 조합, 테스트케이스 채점, Runner 호출 병목이 드러나지 않는다.

| 데이터 | 권장 개수 | 이유 |
| --- | ---: | --- |
| 학생 계정 | 1개 이상 | k6 로그인에 필요 |
| 문제세트 | 1개 이상 | 문제세트 진입 API 대상 |
| 문제 | 20개 이상 | 문제 목록 조회 비용 확인 |
| 테스트케이스 | 문제당 1~3개 | 제출/채점 비용 확인 |
| 데이터셋 | 1개 이상 | Runner에서 CSV 기반 실행 확인 |
| 문제 진행 상태 | 학생 1명 기준 | 이어풀기 상태 확인 |

시더를 만든다면 다음 조건을 따른다.

| 조건 | 설명 |
| --- | --- |
| 프로필 | `@Profile("loadtest")` |
| 실행 시점 | 애플리케이션 부팅 직후 |
| 방식 | `JdbcTemplate batch` 권장 |
| 중복 방지 | 이미 시드 데이터가 있으면 skip |
| DB | 운영 DB가 아니라 loadtest DB만 사용 |

현재 시더 클래스:

```text
src/main/java/com/wanted/codebombalms/problems/set/infrastructure/loadtest/ProblemsLoadTestSeeder.java
```

시더 기본값:

| 항목 | 값 |
| --- | --- |
| 로그인 계정 | `u01@test.com` / `Test1234!` |
| 문제세트 ID | `4001` |
| 첫 번째 문제 ID | `5001` |
| 문제 수 | 20개 |
| 테스트케이스 | 문제당 1개 |
| 데이터셋 | `employee_performance.csv` |

### 5-2. 시드 확인 SQL

```bash
docker compose exec mysql mysql -ulms_loadtest -ploadtest lms_loadtest \
  -e "select count(*) from problem_set;"
```

```bash
docker compose exec mysql mysql -ulms_loadtest -ploadtest lms_loadtest \
  -e "select count(*) from problem;"
```

```bash
docker compose exec mysql mysql -ulms_loadtest -ploadtest lms_loadtest \
  -e "select count(*) from problem_test_case;"
```

기대값:

| 테이블 | 기대값 |
| --- | ---: |
| `problem_set` | 1 이상 |
| `problem` | 20 이상 |
| `problem_test_case` | 20 이상 |
| `problem_dataset` | 1 이상 |
| `users` | 테스트 학생 1명 이상 |

### 5-3. baseline 실행

문제세트 진입 baseline:

```bash
MSYS_NO_PATHCONV=1 docker compose run --rm \
  -e PROBLEM_SET_ID=4001 \
  -e RESULT_NAME=problem-set-entry-before \
  k6 run -o experimental-prometheus-rw /scripts/problems/01-problem-set-entry-baseline.js
```

코드 제출/채점 baseline:

```bash
MSYS_NO_PATHCONV=1 docker compose run --rm \
  -e PROBLEM_ID=5001 \
  -e RESULT_NAME=submission-before \
  k6 run -o experimental-prometheus-rw /scripts/problems/02-submission-baseline.js
```

### 5-4. 결과 저장 위치

k6 결과는 `handleSummary`에 의해 아래 위치에 저장된다.

```text
monitoring/k6/results/problem-set-entry-before-summary.md
monitoring/k6/results/problem-set-entry-before-summary.json
monitoring/k6/results/submission-before-summary.md
monitoring/k6/results/submission-before-summary.json
```

---

## 6단계 준비 - PromQL / LogQL 후보

### 제출/채점 평균 처리 시간

```promql
rate(submission_grading_duration_seconds_sum[1m])
/
rate(submission_grading_duration_seconds_count[1m])
```

### Runner 평균 실행 시간

```promql
rate(code_execution_runner_duration_seconds_sum[1m])
/
rate(code_execution_runner_duration_seconds_count[1m])
```

### 문제세트 진입 평균 처리 시간

```promql
rate(problem_set_entry_duration_seconds_sum[1m])
/
rate(problem_set_entry_duration_seconds_count[1m])
```

### 제출 실패 증가량

```promql
rate(submission_failed_total[1m])
```

### 제출/채점 완료 로그

```logql
{job="lms"} |= "event=submission_completed"
```

### Runner 호출 완료 로그

```logql
{job="lms"} |= "event=code_execution_runner_completed"
```

### 문제세트 진입 로그

```logql
{job="lms"} |= "event=problem_set_entered"
```

### 1초 이상 걸린 Problems 요청

```logql
{job="lms"}
  |= "event=request_completed"
  |= "/api/v1/problem"
  | regexp "durationMs=([1-9][0-9]{3,})"
```
