# k6 Result - submission-external-call-after-transaction-split

## Summary

| Metric | Value |
| --- | ---: |
| http_reqs | 600 |
| iterations | 300 |
| checks success rate | 100.00% |
| http_req_failed | 0.00% |
| data_received bytes | 641295 |
| data_sent bytes | 355390 |

## Duration Metrics

| Metric | avg(ms) | min(ms) | med(ms) | p90(ms) | p95(ms) | p99(ms) | max(ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| http_req_duration | 879.06 |  | 916.04 | 1694.87 | 1877.74 | 1990.89 | 2015.04 |
| http_req_waiting | 878.56 |  | 915.61 | 1694.58 | 1876.97 | 1990.62 | 2014.76 |
| http_req_blocked | 0.93 |  | 0.00 | 0.01 | 11.48 | 16.34 | 19.39 |
| http_req_connecting | 0.92 |  | 0 | 0 | 11.33 | 16.29 | 19.33 |

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
| status is 200 | 300 pass / 0 fail |
| response has data | 300 pass / 0 fail |
| problem id is valid | 300 pass / 0 fail |
| submission id exists | 300 pass / 0 fail |
| test case count is 5 | 300 pass / 0 fail |
| result is wrong answer | 300 pass / 0 fail |

## How To Compare

| Compare Point | What To Look For |
| --- | --- |
| p95 | 사용자 대부분이 체감하는 지연 시간 악화 여부 |
| http_req_failed | 4xx/5xx 또는 check 실패 증가 여부 |
| http_req_waiting | 서버 처리나 DB 처리 지연 가능성 |
| Prometheus | 서버 내부 HTTP/custom metric 추세 |
| Loki | 느린 요청의 traceId와 event 로그 |

