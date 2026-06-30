# k6 Result - submission-external-call-before-transaction-split

## Summary

| Metric | Value |
| --- | ---: |
| http_reqs | 600 |
| iterations | 300 |
| checks success rate | 43.33% |
| http_req_failed | 28.33% |
| data_received bytes | 584795 |
| data_sent bytes | 355390 |

## Duration Metrics

| Metric | avg(ms) | min(ms) | med(ms) | p90(ms) | p95(ms) | p99(ms) | max(ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| http_req_duration | 1658.24 |  | 868.45 | 3312.24 | 3660.02 | 4565.87 | 4596.38 |
| http_req_waiting | 1654.46 |  | 867.87 | 3311.90 | 3659.31 | 4565.30 | 4595.36 |
| http_req_blocked | 1.26 |  | 0.00 | 4.30 | 7.30 | 9.25 | 11.03 |
| http_req_connecting | 1.22 |  | 0 | 4.27 | 7.21 | 9.20 | 10.93 |

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
| status is 200 | 130 pass / 170 fail |
| response has data | 130 pass / 170 fail |
| problem id is valid | 130 pass / 170 fail |
| submission id exists | 130 pass / 170 fail |
| test case count is 5 | 130 pass / 170 fail |
| result is wrong answer | 130 pass / 170 fail |

## How To Compare

| Compare Point | What To Look For |
| --- | --- |
| p95 | 사용자 대부분이 체감하는 지연 시간 악화 여부 |
| http_req_failed | 4xx/5xx 또는 check 실패 증가 여부 |
| http_req_waiting | 서버 처리나 DB 처리 지연 가능성 |
| Prometheus | 서버 내부 HTTP/custom metric 추세 |
| Loki | 느린 요청의 traceId와 event 로그 |

