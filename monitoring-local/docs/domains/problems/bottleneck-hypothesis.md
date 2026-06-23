# Problems/Reward/Ranking/Badge 도메인 병목 가설

> 프로세스: [`../../PROCESS.md`](../../PROCESS.md) · 컨벤션: [`../../CONVENTION.md`](../../CONVENTION.md)
>
> 목적은 "어디가 느릴 것 같다"를 코드 구조 기준으로 먼저 가정하고, 이후 k6·Prometheus·Grafana·Loki로 검증하는 것이다.  
> 제출/채점은 계속 관찰하되, 이번 문서는 담당 도메인 중 실제 서비스 병목 가능성이 높은 후보를 다시 정리한다.  
> Recommendation 도메인은 담당 범위가 아니므로 제외한다.

---

## 1. 담당 범위

| 도메인 | 대표 기능 | 병목 관점 |
|---|---|---|
| Problems | 문제세트 진입, 문제세트 등록·수정, 데이터셋 다운로드 URL | 조회 조합, 진행 상태 생성, GCS Signed URL |
| Submission | 코드 제출, 제출 기록, 채점 결과 조회 | 외부 Runner, 테스트케이스 반복 실행, 제출 저장 |
| Reward Point | 포인트 지급, 지급 실패 복구 스케줄러 | 이벤트 후속 처리, 재시도 큐, 중복 지급 방지 |
| Ranking | 전체/주간/내 포인트 랭킹 조회 | 집계, 윈도우 함수, 정렬, 주간 포인트 계산 |
| Badge | 배지 등록·수정·삭제, 내 배지, 배지 자동 획득 | 포인트 이벤트 후 동기화, 이미지 Signed URL |

---

## 2. API/작업 유형 분류

| 유형 | 대상 | 병목 성격 | 측정 방향 |
|---|---|---|---|
| 조회/집계형 | 랭킹 조회, 문제세트 진입, 내 배지 조회, 추천 코스 조회 | 인덱스, 정렬, N+1, 응답 payload | p95/p99, DB 쿼리 수, Hikari active/pending |
| 외부 연동형 | 코드 Runner, GCS Signed URL, GCS 이미지 저장 | 외부 응답 지연, timeout, 재시도 | 외부 호출 duration, 실패율, timeout count |
| 쓰기/트랜잭션형 | 포인트 지급, 배지 획득, 문제세트 수정, 추천 코스 저장 | 락, 중복 처리, 커넥션 점유 | transaction duration, lock wait, failed/retry count |
| 스케줄러/배치형 | 포인트 복구, 배지 하드 삭제 | 한 번에 처리하는 row 수, 중복 실행, backlog | batch duration, processed count, backlog count |
| 단순 CRUD형 | 카테고리, 힌트, 단건 테스트케이스 관리 | 병목 가능성 낮음 | 기본 정상성만 확인 |

---

## 3. 병목 후보 우선순위

| 우선순위 | 후보 | 관련 API/작업 | 왜 병목 후보인가 | 코드 근거 | 관찰 지표 |
|---|---|---|---|---|---|
| 1 | 랭킹 조회 | `GET /api/v1/rankings/points`, `GET /api/v1/rankings/points/weekly`, `GET /api/v1/rankings/points/me` | 전체 랭킹과 내 랭킹이 `dense_rank()` 윈도우 함수로 순위를 계산한다. 주간 랭킹은 `point_history`를 7일 기준으로 집계하고 정렬한다. 사용자와 포인트 이력이 늘면 full scan, group by, sort 비용이 커질 수 있다. | `RankingService`, `RankingQueryAdapter` | API p95/p99, `user_point`/`point_history` rows scanned, Hikari active/pending, slow query |
| 2 | 배지 자동 획득 동기화 | 포인트 지급 이후 `PointGrantedEvent` 후속 처리 | 정답 제출로 포인트가 지급될 때마다 사용자의 총 포인트 기준으로 획득 가능한 배지를 조회하고, 이미 보유한 배지와 비교해 저장한다. 포인트 지급이 몰리거나 배지 수가 많아지면 이벤트 후속 처리 비용이 커진다. | `BadgePointGrantedEventListener`, `MyBadgeService.sync`, `UserBadgePersistenceAdapter` | badge sync duration, newly earned count, active badge count, user_badge insert count, event failure log |
| 3 | 문제세트 진입 | `GET /api/v1/problem-sets/{problemSetId}` | 학생이 문제풀이 화면에 들어갈 때 문제세트, 진행 상태, 문제 목록, 시작 코드, 최신 제출 상태를 조합한다. 현재 문제마다 `loadStartCode(problemId)`를 호출하므로 문제 수가 늘면 N+1 후보가 된다. 처음 진입 시 진행 상태 생성과 started count 증가도 함께 발생한다. | `ProblemSetEntryService` | entry p95/p99, problem count, query count, `event=problem_set_entered durationMs`, Hikari active |
| 4 | 포인트 지급 실패 복구 | 1분 주기 PENDING 작업 재처리 | 포인트 지급 실패 시 `point_reward_task`에 PENDING/FAILED 상태가 쌓이고, 스케줄러가 복구 가능한 작업을 batch로 가져와 처리한다. 장애 이후 backlog가 많아지면 복구 스케줄러가 DB와 포인트 지급 로직을 압박할 수 있다. | `PointRewardTaskService`, `PendingPointRewardRecoveryService`, `PointRewardRecoveryScheduler` | pending task count, recover duration, processed count, retry count, permanent failed count |
| 5 | 내 배지 조회/대표 배지 변경 | `GET /api/v1/badges/me`, `PATCH /api/v1/badges/me/{badgeId}/equip` | 내 배지 조회는 user_badge 목록을 읽고 badge 정보를 다시 조회한 뒤 이미지 Signed URL을 만든다. 대표 배지 변경은 기존 장착 배지를 해제하고 선택 배지를 장착한다. 보유 배지가 많아지면 조회/저장 대상이 늘어난다. | `MyBadgeService.getMyBadges`, `MyBadgeService.equipBadge` | my badge p95, signed URL duration, equipped badge count, saveAll duration |
| 6 | 문제세트 수정/데이터셋 수정 | `PUT /api/v1/problems/{problemSetId}`, `PUT /api/v1/problems/{problemSetId}/with-dataset` | 운영자 API라 트래픽은 낮지만 문제, 힌트, 테스트케이스, 데이터셋을 한 흐름에서 동기화한다. CSV 업로드가 포함되면 GCS 업로드 실패 보상과 DB 변경이 엮인다. | `ProblemSetUpdateService`, `ProblemSetWithDatasetUpdateService`, `ProblemDatasetCommandService` | update duration, GCS upload duration, rollback log, validation error count |
| 7 | 데이터셋 다운로드 URL 발급 | `POST /api/v1/problem-sets/{problemSetId}/dataset/download-url` | 클릭할 때마다 접근 권한 검증, 활성 데이터셋 조회, GCS Signed URL 생성을 수행한다. CSV 용량과 무관하게 URL 서명 생성은 가볍지만, 다운로드 버튼 광클이나 GCS 인증 문제 발생 시 장애 신호가 될 수 있다. | `DatasetDownloadUrlService`, `GcsDatasetAccessUrlAdapter` | issue duration, access denied count, signed URL failure count |

---

## 4. 이번 M4에서 먼저 볼 후보

| 순위 | 후보 | 선택 이유 | 권장 측정 |
|---|---|---|---|
| 1 | 랭킹 조회 | 사용자와 포인트 이력이 늘수록 자연스럽게 느려지는 집계형 API다. 현재 SQL도 윈도우 함수와 주간 집계를 요청마다 수행한다. | k6 조회 시나리오, DB slow query, `point_history` 인덱스 확인 |
| 2 | 배지 자동 획득 동기화 | 포인트 지급 이벤트 뒤에 자동으로 붙는 후속 작업이라 사용자가 직접 호출하지 않아도 트래픽에 비례해 늘어난다. 실패해도 사용자는 바로 모를 수 있어 로그/메트릭이 중요하다. | 포인트 지급 후 badge sync duration, newly earned count, failure log |
| 3 | 문제세트 진입 | 이미 k6에서 측정했을 때 큰 실패는 아니지만, 문제 수가 많아지면 startCode 조회 N+1 후보가 남아 있다. | entry p95, problem count, query count |
| 4 | 포인트 지급 실패 복구 | 평소에는 조용하지만 장애 이후 PENDING 작업이 쌓이면 한 번에 복구 부하가 몰릴 수 있다. | pending backlog, recovery duration, retry/permanent failed count |

제출/채점은 현재도 중요한 외부 연동형 흐름이지만, 최근 측정에서 실패율 0%이고 p95가 기준 근처까지 내려왔다. 따라서 이번 문서에서는 "계속 관찰 대상"으로 두고, 1순위 병목 후보에서는 제외한다.

---

## 5. 당장 필요해 보이는 로그/메트릭 후보

| 대상 | 추가하면 좋은 로그/메트릭 | 의미 |
|---|---|---|
| 랭킹 조회 | `event=ranking_list_queried type=total/weekly resultCount durationMs` | 랭킹 조회가 실제로 얼마나 걸리는지, 페이지 크기와 함께 확인 |
| 내 랭킹 조회 | `event=my_ranking_queried userId durationMs` | 특정 사용자 순위 계산 비용 확인 |
| 배지 동기화 | `event=badge_sync_completed userId totalPoint newlyEarnedBadgeCount durationMs` | 포인트 지급 후 배지 후속 처리 비용 확인 |
| 배지 동기화 실패 | `event=badge_sync_failed userId totalPoint exceptionType` | 포인트는 지급됐지만 배지 획득이 누락될 수 있는 상황 추적 |
| 포인트 복구 | `event=point_reward_recovery_completed processedCount durationMs` | 복구 스케줄러 backlog 처리량 확인 |
| 문제세트 진입 | `event=problem_set_entered problemCount solvedProblemCount durationMs` | 이미 존재. 문제 수와 응답 시간 상관관계 확인 |

---

## 6. 병목이 아니라고 보는 것

| 대상 | 이유 |
|---|---|
| 카테고리 단건/목록 조회 | 단순 CRUD에 가깝고 데이터 크기도 제한적이다. |
| 힌트 단건 조회 | 문제풀이 화면의 핵심 병목보다는 보조 조회에 가깝다. |
| 테스트케이스 운영자 단건 관리 | 운영자 트래픽이며 대량 동시 요청 가능성이 낮다. |
| 데이터셋 다운로드 URL 발급 | GCS 장애 감지는 필요하지만, 현재 구조상 대용량 다운로드 자체는 서버를 거치지 않는다. |

---

## 7. 다음 단계

| 단계 | 작업 |
|---|---|
| 2단계 | `metrics.md`에 랭킹/배지/포인트 복구/문제세트 진입별 로그·메트릭 설계 추가 |
| 3단계 | API 유형별 성공 기준 정리. 랭킹은 조회형, 배지 동기화는 후속 이벤트형, 포인트 복구는 스케줄러형으로 분리 |
| 4단계 | k6 시나리오 추가. 랭킹 조회와 문제세트 진입은 HTTP 부하, 배지/포인트 복구는 시드+이벤트/스케줄러 실행 방식 검토 |
| 5단계 | loadtest seed 보강. 사용자, 포인트 이력, 배지 조건, 지급 실패 작업 데이터를 충분히 넣어 병목을 드러낸다 |
