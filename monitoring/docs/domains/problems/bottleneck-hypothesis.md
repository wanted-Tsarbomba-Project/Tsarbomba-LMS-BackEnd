# Problems 도메인 병목 가설 (Phase 1 파일럿 / 7단계 중 1단계)

> 프로세스: [`../../PROCESS.md`](../../PROCESS.md) · 컨벤션: [`../../CONVENTION.md`](../../CONVENTION.md)
>
> 이 문서는 부하테스트 전에 "무엇이 느릴 것인가"를 먼저 가설로 세우는 산출물이다.  
> 목적은 무작정 부하를 주는 것이 아니라, API 유형별로 병목 후보를 정하고 측정 결과로 검증하는 것이다.

---

## 도메인 유형 분류

Problems 도메인은 하나의 유형으로만 보기 어렵다. 문제 목록 조회, 문제풀이 진입, 코드 실행, 제출/채점, 운영자 관리 API가 섞여 있다.

| 유형 | 해당 흐름 | 병목 성격 | 테스트 방향 |
|---|---|---|---|
| 조회/집계형 | 문제세트 목록, 문제세트 진입, 진행 상태, 결과 조회, 추천 코스 조회 | 인덱스, 조인, N+1, 응답 payload 크기 | 조회 API p95와 DB 쿼리 수 확인 |
| 외부연동형 | 코드 실행, 코드 제출/채점, 데이터셋 다운로드 URL 발급 | Cloud Run Python Runner, GCS Signed URL, 외부 HTTP 지연 | 외부 지연 시간과 타임아웃 비율 확인 |
| 쓰기/트랜잭션형 | 코드 제출, 문제세트 등록/수정, 추천 코스 연결 저장 | 테스트케이스 실행 결과 저장, 진행 상태 갱신, 포인트 이벤트 발행 | Hikari active, transaction duration, 실패율 확인 |
| 단순 CRUD형 | 카테고리 조회, 힌트 조회, 테스트케이스 단건 관리 | 병목 가능성 낮음 | baseline만 확인하고 억지 최적화 금지 |

---

## 병목 가설표

| # | 대상 API | 유형 | 병목 가설 | 근거 코드 위치 | 관찰 지표 | 성공 기준 |
|---|---|---|---|---|---|---|
| 1 | `POST /api/v1/problems/{problemId}/submissions` | 외부연동 + 쓰기/트랜잭션 | 가장 강한 병목 후보. 제출 1회마다 테스트케이스를 불러오고, 사용자 코드를 Python Runner로 실행한 뒤 결과 저장, 진행 상태 갱신, 포인트 이벤트 발행까지 이어진다. Runner 응답 지연 또는 테스트케이스 수 증가가 p95를 크게 올릴 가능성이 높다. | `submission/application/service/SubmissionService`, `submission/application/service/CodeGradingService`, `problems/execution/infrastructure/runner/CloudRunCodeRunnerAdapter` | `http_server_requests_seconds{uri="/api/v1/problems/{problemId}/submissions"}` p95, Runner 호출 duration, Hikari active, `event=request_completed durationMs`, 4xx/5xx 비율 | VU 20~50에서 p95가 5초 이내, 실패율 1% 미만. Runner 타임아웃은 별도 집계 |
| 2 | `POST /api/v1/code-problems/{problemId}/executions` | 외부연동형 | 단순 코드 실행이지만 Runner와 GCS 데이터셋 다운로드가 포함되면 외부 지연이 대부분을 차지한다. 서버 내부 최적화보다 Runner 연결/응답 시간, timeout 설정 검증이 핵심이다. | `problems/execution/application/service/CodeExecutionService`, `problems/execution/infrastructure/runner/CloudRunCodeRunnerAdapter` | 코드 실행 API p95, Runner HTTP duration, timeout count, Loki errorMessage | p95가 기본 실행 제한 시간 근처에서 안정적이고, timeout/connection error가 1% 미만 |
| 3 | `GET /api/v1/problem-sets/{problemSetId}` | 조회/집계형 | 문제세트 진입 시 문제 목록, 진행 상태, 최신 제출, startCode를 함께 구성한다. 문제 수가 많거나 사용자별 진행/제출 상태 조회가 늘어나면 조회 비용이 증가할 수 있다. | `problems/set/application/service/ProblemSetEntryService`, `problems/set/infrastructure/persistence/ProblemSetEntryPersistenceAdapter`, `problems/set/application/service/ProblemStartCodeService` | API p95, DB query count, Hikari active, 응답 payload size | 문제 20개 이상 시에도 p95 500ms~1s 이내. N+1이 보이면 batch/projection 검토 |
| 4 | `GET /api/v1/problems/{problemSetId}` | 조회/집계형 | 운영자 수정 화면은 문제세트 기본 정보, 문제 목록, 힌트, 데이터셋, 테스트케이스를 한 번에 내려준다. 현재 힌트/테스트케이스는 일괄 조회 구조로 개선됐지만, 문제 수와 테스트케이스 수가 많으면 payload와 조합 비용이 커질 수 있다. | `problems/set/application/service/ProblemSetForUpdateQueryService` | API p95, 응답 크기, DB query count, memory allocation 추세 | 문제 20개, 테스트케이스 40개 수준에서 p95 1초 이내 |
| 5 | `GET /api/v1/problem-sets` | 조회형 | 전체 문제세트 목록 조회는 단순 조회에 가깝다. 다만 카테고리 필터, 문제세트 수 증가, 정렬 조건에 따라 인덱스 영향이 생길 수 있다. 병목 후보는 낮다. | `problems/set/application/service/ProblemSetQueryService`, `problems/set/infrastructure/persistence/ProblemSetPersistenceAdapter` | API p95, DB rows scanned, Hikari active | VU 50에서 p95 500ms 이내. 병목 없으면 baseline 충족으로 종료 |
| 6 | `PUT /api/v1/problems/{problemSetId}` / `PUT /api/v1/problems/{problemSetId}/with-dataset` | 쓰기/트랜잭션 + 외부연동 | 운영자 수정은 빈번한 사용자 트래픽 API는 아니지만, 문제/힌트/테스트케이스 동기화와 데이터셋 업로드가 함께 발생한다. CSV 업로드가 포함되면 GCS 지연과 트랜잭션 보상 로직이 병목 후보가 된다. | `problems/set/application/service/ProblemSetUpdateService`, `ProblemSetWithDatasetUpdateService`, `problems/dataset/application/service/ProblemDatasetCommandService` | 요청 duration, GCS upload duration, transaction duration, 실패 로그 | 운영자 API이므로 고부하 최적화 대상은 낮음. 기능 안정성 위주로 5xx 0% 확인 |
| 7 | `PUT /api/v1/problems/{problemId}/recommended-courses` | 쓰기/트랜잭션형 | 추천 코스 연결 저장은 기존 연결 삭제 후 요청 목록 기준 재저장한다. 코스 수가 많지 않다는 정책이면 병목 가능성은 낮다. 단, 같은 문제에 동시 저장이 몰리면 마지막 요청 기준으로 덮어쓰기 되는 정책 확인이 필요하다. | `problems/recommendation/application/service/ProblemRecommendedCourseCommandService`, `problems/recommendation/infrastructure/persistence/ProblemRecommendedCoursePersistenceAdapter` | API p95, duplicate/validation error, DB lock wait | 단순 운영자 API로 baseline만 확인. 병목 없음 결론 가능 |
| 8 | `GET /api/v1/problems/{problemId}/recommended-courses` | 조회형 | 학생 문제풀이 화면에서 호출되는 추천 코스 조회다. 현재 연결된 추천 courseId와 ACTIVE 코스 목록을 매칭한다. 코스 전체 목록이 커지면 불필요한 전체 조회가 병목이 될 수 있다. | `problems/recommendation/application/service/ProblemRecommendedCourseQueryService`, `problems/recommendation/infrastructure/course/RecommendationCourseAdapter` | API p95, course rows scanned, DB query count | 코스 수 증가 시에도 p95 500ms 이내. 필요 시 연결된 courseId 기준 IN 조회로 개선 |

---

## 1차 집중 대상

이번 Problems 도메인의 메인 부하테스트 대상은 아래 2개로 잡는다.

| 우선순위 | API | 이유 |
|---|---|---|
| 1 | `POST /api/v1/problems/{problemId}/submissions` | 사용자 코드 실행, 테스트케이스 채점, DB 저장, 진행 상태 갱신, 포인트 이벤트가 연결된 핵심 트랜잭션이다. 외부 Runner 지연과 DB 쓰기 병목을 동시에 볼 수 있다. |
| 2 | `GET /api/v1/problem-sets/{problemSetId}` | 문제풀이 화면 진입 API로 사용자 체감에 직접 영향을 준다. 문제 수가 늘어날 때 N+1 또는 payload 병목이 생기는지 확인하기 좋다. |

운영자 관리 API와 추천 코스 저장 API는 중요하지만 일반 사용자 트래픽이 낮고, 단순 CRUD 또는 관리자 트랜잭션 성격이 강하므로 1차 부하테스트에서는 보조 관찰 대상으로 둔다.

---

## 측정 전 확인할 데이터 조건

| 항목 | 권장 조건 |
|---|---|
| 문제세트 | 최소 1개 이상, 가능하면 문제 6~20개 포함 |
| 테스트케이스 | 문제당 1~3개 이상 |
| 제출 사용자 | 학생 계정 1개 이상 |
| 데이터셋 | CSV 데이터셋이 연결된 문제세트 1개 이상 |
| 추천 코스 | 문제 1개에 추천 코스 1~3개 연결 |

데이터가 너무 적으면 병목이 보이지 않는다. 특히 N+1이나 payload 문제는 문제 수와 테스트케이스 수가 늘어났을 때 드러난다.

---

## 다음 단계

- 2단계: Problems 도메인 전용 커스텀 metric/log 후보를 정리한다. (`metrics.md`)
- 4단계: `monitoring/k6/scripts/problems/` 아래에 문제세트 진입과 코드 제출 시나리오를 작성한다.
- 5단계: loadtest DB에 문제세트, 문제, 테스트케이스, 학생 계정, 데이터셋 더미를 넣고 baseline을 측정한다.
