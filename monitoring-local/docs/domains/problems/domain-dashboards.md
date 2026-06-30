# Problem / Submission / Ranking / Badge / Reward 대시보드

## 1. DDD 기준 분리

| 바운디드 컨텍스트 | 대시보드 | 핵심 관측 대상 |
| --- | --- | --- |
| Problem | `[LMS][도메인 상세] 문제` | 문제세트 진입 단계, 진행도 조회, 코드 실행기 실패 |
| Submission | `[LMS][도메인 상세] 제출` | 제출 준비, 채점, 저장, 실패 로그 |
| Ranking | `[LMS][도메인 상세] 랭킹` | 내 랭킹 없음, 랭킹 정합성, 대표 뱃지 URL 후보 |
| Badge | `[LMS][도메인 상세] 뱃지` | 뱃지 동기화 단계, 신규 획득 로그, 이미지 저장소 오류 |
| Reward | `[LMS][도메인 상세] 보상 포인트` | 포인트 지급 작업, 재시도, 영구 실패, 복구 스케줄러 |

전용 대시보드는 `monitoring-local/grafana/provisioning/dashboards/`에 있다.

## 2. 모든 코드에 커스텀 메트릭과 로그가 필요한가

필요하지 않다. 관측은 아래 3개 계층으로 나눈다.

| 계층 | 현재 상태 | 답하는 질문 |
| --- | --- | --- |
| 요청 메트릭 + `domain` 태그 | 공통 자동 수집 | 어느 컨텍스트와 API가 느리거나 실패하는가 |
| 공통 요청 로그 + `traceId` | 공통 자동 수집 | 실패하거나 느린 요청 하나가 무엇인가 |
| 커스텀 Timer/Counter/Gauge + 이벤트 로그 | 핵심 흐름에만 추가 | 내부의 어느 구간과 이유가 문제인가 |

공용 대시보드는 요청량, 오류율, URI별 평균 응답 시간, 리소스 상태를 보여주는 1차 탐지 화면이다. 도메인 상세 대시보드는 이 공용 지표를 반복하지 않고, 내부 처리 단계와 비즈니스 이벤트 로그만 보여준다.

Reward는 요청 컨트롤러가 없는 비동기 컨텍스트이므로 공용 요청 지표가 생성되지 않는다. 지급 작업의 적체와 실패를 보려면 커스텀 메트릭과 구조화 로그가 필수다.

## 3. 현재 계측 상태

| 컨텍스트 | 자동 HTTP | 커스텀 메트릭 | 구조화 이벤트 로그 | 판단 |
| --- | --- | --- | --- | --- |
| Problem | 공용 대시보드에서 확인 | 문제세트 진입 단계별 Timer 있음 | 진입/실패, 코드 실행기 완료/실패 있음 | 상세 대시보드에서 원인 분해 가능 |
| Submission | 공용 대시보드에서 확인 | prepare/grading/save/total Timer 있음 | 완료/실패 있음 | 상세 대시보드에서 원인 분해 가능 |
| Ranking | 공용 대시보드에서 확인 | 없음 | 공통 요청 로그만 있음 | 상세 대시보드는 정합성/오류 로그 중심 |
| Badge | 공용 대시보드에서 확인 | 동기화 단계별 Timer 있음 | 동기화 완료 로그 있음 | 상세 대시보드에서 원인 분해 가능 |
| Reward | 없음 | pending/scheduled/processed/duration 있음 | 작업 생성·완료·재시도·실패 로그 있음 | 비동기 처리 전용 대시보드 사용 가능 |

## 4. Reward 메트릭 계약

Reward 대시보드는 아래 이름을 사용한다.

| 메트릭 | 종류 | 의미 |
| --- | --- | --- |
| `reward_point_task_pending` | Gauge | 현재 PENDING 지급 작업 수 |
| `reward_point_task_scheduled_total` | Counter | 생성된 지급 작업 누적 수 |
| `reward_point_task_processed_total{result}` | Counter | `completed`, `retry`, `failed` 결과별 처리 수 |
| `reward_point_task_process_duration` | Timer | 지급 작업 한 건의 처리 시간 |

`reward_point_task_pending`은 Prometheus 스크레이프마다 DB를 조회하지 않는다. 별도 스케줄러가 기본 30초마다 `PENDING` 건수를 조회해 Gauge를 갱신한다. 이 방식은 DB 부하와 수집기 결합을 줄이는 대신 대기 건수가 최대 30초 늦게 반영될 수 있다.

권장 이벤트 로그는 다음과 같다.

```text
event=reward_point_task_scheduled submissionId=... point=...
event=reward_point_task_completed submissionId=... retryCount=... durationMs=...
event=reward_point_task_retry_scheduled submissionId=... retryCount=... reason=...
event=reward_point_task_failed submissionId=... retryCount=... reason=...
event=reward_point_recovery_completed processedCount=... durationMs=...
```

`userId`, `problemId`, `submissionId`, 예외 메시지는 메트릭 태그로 사용하지 않고 로그 본문에만 기록한다. 메트릭의 `result`와 `reason`은 미리 정한 낮은 카디널리티 값만 사용한다.

## 5. 선택적 추가 계측

| 컨텍스트 | 추가 시점 | 후보 |
| --- | --- | --- |
| Ranking | API 평균·최대 지연이 상승하고 DB 집계와 URL 생성 중 원인을 구분해야 할 때 | `ranking_query_duration`, `ranking_badge_url_duration` |
| Badge | 실패 원인과 신규 획득량을 시계열로 집계해야 할 때 | `badge_sync_processed_total{result}`, `badge_sync_newly_earned` |
| Submission | HTTP 상태와 별개로 채점 결과/실패 이유가 필요할 때 | `submission_processed_total{result}`, `submission_failed_total{reason}` |

## 6. 공용/상세 대시보드 역할 분리

운영 흐름은 다음 순서로 본다.

1. `[LMS 공용 도메인 대시보드]`에서 도메인별 요청량, 오류율, URI별 평균 응답 시간, 리소스 상태를 확인한다.
2. 이상이 발생한 도메인의 `[LMS][도메인 상세] ...` 대시보드로 이동한다.
3. 상세 대시보드에서 내부 처리 단계, 비즈니스 이벤트 로그, 실패 로그로 원인을 좁힌다.

이 분리는 공용 대시보드와 상세 대시보드가 같은 패널을 반복하지 않기 위한 결정이다. 공용은 "어디가 이상한가"를 답하고, 상세는 "왜 이상한가"를 답한다.

## 7. Import

로컬 구성은 datasource UID를 `prometheus`, `loki`로 고정한다. Grafana의 `Dashboards > New > Import`에서 JSON 파일을 업로드하거나, `monitoring-local`을 다시 시작하면 provisioning으로 자동 로드된다.

다른 Grafana 환경에 Import할 때는 Prometheus와 Loki datasource UID가 다르면 JSON의 datasource UID를 해당 환경에 맞게 변경한다.
