# k6 Result - 01-email-check

## Summary

| Metric | Value |
| --- | ---: |
| http_reqs | 752 |
| iterations | 752 |
| checks success rate | 84.31% |
| http_req_failed | 15.69% |
| data_received bytes | 433538 |
| data_sent bytes | 111217 |

## Duration Metrics

| Metric | avg(ms) | min(ms) | med(ms) | p90(ms) | p95(ms) | p99(ms) | max(ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| http_req_duration | 2479.03 | 156.92 | 2815.78 | 3271.31 | 3471.10 | 3831.36 | 4901.47 |
| http_req_waiting | 2478.76 | 156.79 | 2815.66 | 3271.13 | 3470.90 | 3831.22 | 4901.35 |
| http_req_blocked | 0.59 | 0.00 | 0.01 | 2.69 | 2.94 | 3.33 | 3.95 |
| http_req_connecting | 0.56 | 0 | 0 | 2.58 | 2.82 | 3.21 | 3.82 |

## Metric Meaning

| Value | Meaning |
| --- | --- |
| avg | 전체 요청 시간의 산술 평균입니다. outlier의 영향을 받을 수 있습니다. |
| min | 가장 빠른 요청 시간입니다. 정상 동작의 하한선을 볼 때 사용합니다. |
| med | 중앙값입니다. 요청의 절반은 이 값보다 빠르고 절반은 느립니다. |
| p90 | 90% 요청이 이 값 이하로 완료됩니다. |
| p95 | 95% 요청이 이 값 이하로 완료됩니다. 주요 합격 기준입니다. |
| p99 | 99% 요청이 이 값 이하로 완료됩니다. tail latency 관찰에 사용합니다. |
| max | 가장 느린 요청 시간입니다. 단일 outlier 여부를 확인할 때 사용합니다. |

## Checks

| Check | Result |
| --- | --- |
| status is 200 | 634 pass / 118 fail |
| has available field | 634 pass / 118 fail |

## How To Compare

| Compare Point | What To Look For |
| --- | --- |
| p95 | 사용자 대부분이 체감하는 지연 시간 악화 여부 |
| http_req_failed | 4xx/5xx 또는 check 실패 증가 여부 |
| http_req_waiting | 서버 처리나 DB 처리 지연 가능성 |
| Prometheus | 서버 내부 HTTP/custom metric 추세 |
| Loki | 느린 요청의 traceId와 event 로그 |

