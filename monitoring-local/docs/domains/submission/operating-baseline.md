# Submission 운영 기준선

## 목적

Submission 대시보드는 단순히 응답 시간이 느린지 보는 화면이 아니라, 운영 중 이상 징후가 생겼을 때 원인 후보를 빠르게 좁히기 위한 화면이다.

이 기준은 이전 제출 API 부하 테스트 결과를 기준으로 잡았다.

| 구간 | 실패율 | 유효 요청률 | k6 p95 | k6 p99 | Hikari pending |
| --- | ---: | ---: | ---: | ---: | ---: |
| 트랜잭션 분리 전 | 28.33% | 43.3% | 3.66s | 4.56s | 약 40 |
| 트랜잭션 분리 후 | 0% | 100% | 1.88~1.95s | 1.99~2.00s | 0 |

따라서 현재 운영 대시보드에서는 개선 후 상태를 정상 기준선으로 둔다.

## 운영 기준

| 지표 | 정상 | 경고 | 위험 | 해석 |
| --- | --- | --- | --- | --- |
| Submit 평균 응답 시간 | 2s 미만 | 2s 이상 | 3s 이상 | 사용자가 제출 후 기다리는 시간 |
| 5xx Error Rate | 1% 미만 | 1% 이상 | 5% 이상 | 서버 장애 또는 런타임 실패 |
| Hikari Pending | 0 | 1 이상 지속 | 5 이상 | DB 커넥션 대기 발생 |
| DB Pool Saturation | 70% 미만 | 70% 이상 | 90% 이상 | DB 커넥션 풀이 꽉 차는 중인지 |
| Prepare Avg | 100ms 미만 | 100ms 이상 | 300ms 이상 | 문제, 테스트케이스, 데이터셋 조회 지연 |
| Grading Avg | 1.8s 미만 | 1.8s 이상 | 2.5s 이상 | 외부 채점 실행 지연 |
| Save Avg | 100ms 미만 | 100ms 이상 | 300ms 이상 | 제출/테스트 결과 저장 지연 |

## 이상 상황별 판단

| 관찰 | 가장 의심할 원인 | 먼저 볼 곳 |
| --- | --- | --- |
| 응답 시간 증가 + Hikari pending 증가 | DB 커넥션 부족, 긴 트랜잭션, 락 경합 | Hikari Pool Pressure, Failure Logs |
| 응답 시간 증가 + Grading Avg만 증가 | 외부 runner 지연, 테스트케이스 수 증가, runner timeout | Grading Avg, Runner Failure Logs |
| Prepare Avg 증가 | 문제/테스트케이스/데이터셋 조회 쿼리 문제 | Prepare Avg, Slow Request Logs |
| Save Avg 증가 | submission 저장, test result 저장, DB lock 문제 | Save Avg, Hikari Pending |
| 5xx 증가 | 애플리케이션 예외 또는 외부 runner 실패 | Submission Failures, traceId |
| 4xx 증가 + 5xx 정상 | 클라이언트 요청, 인증, 테스트 데이터 문제 | Request body, user/problem state |

## 운영자가 보는 순서

1. `5xx Error Rate`가 빨간색인지 확인한다.
2. `Hikari Pending`이 0보다 큰 상태로 지속되는지 본다.
3. `Prepare / Grading / Save` 중 어느 구간이 같이 올라갔는지 본다.
4. `Slow Submission Requests`에서 `traceId`를 복사한다.
5. Loki에서 같은 `traceId`로 요청 흐름과 예외 로그를 따라간다.

## 주의점

현재 기준은 로컬 부하 테스트와 mock runner 지연을 기반으로 한 운영 참고선이다. Cloud Run, RDS, 실제 네트워크 환경으로 배포하면 절대 시간은 달라질 수 있다.

다만 실패율, Hikari pending, prepare/grading/save 중 어디가 올라갔는지 보는 방식은 환경이 바뀌어도 유효하다.
