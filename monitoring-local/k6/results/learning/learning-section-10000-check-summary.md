# k6 Result - learning-section-10000-check

## Summary

| Metric | Value |
| --- | ---: |
| http_reqs | 501 |
| iterations | 500 |
| checks success rate | 100.00% |
| http_req_failed | 0.00% |
| data_received bytes | 2080782 |
| data_sent bytes | 201696 |

## Duration Metrics

| Metric | avg(ms) | min(ms) | med(ms) | p90(ms) | p95(ms) | p99(ms) | max(ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| http_req_duration | 62.52 | 21.24 | 27.62 | 39.14 | 554.67 | 630.76 | 654.53 |
| http_req_waiting | 62.05 | 21.09 | 27.14 | 37.95 | 551.74 | 629.51 | 653.60 |
| http_req_blocked | 0.48 | 0.00 | 0.00 | 0.01 | 3.54 | 10.60 | 14.45 |
| http_req_connecting | 0.47 | 0 | 0 | 0 | 3.49 | 10.55 | 14.40 |

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
| status is 200 | 500 pass / 0 fail |
| has page content array | 500 pass / 0 fail |
| page matches request | 500 pass / 0 fail |
| page size is 20 | 500 pass / 0 fail |
| page content has 20 items | 500 pass / 0 fail |

## How To Compare

| Compare Point | What To Look For |
| --- | --- |
| p95 | 사용자 대부분이 체감하는 지연 시간 악화 여부 |
| http_req_failed | 4xx/5xx 또는 check 실패 증가 여부 |
| http_req_waiting | 서버 처리나 DB 처리 지연 가능성 |
| Prometheus | 서버 내부 HTTP/custom metric 추세 |
| Loki | 느린 요청의 traceId와 event 로그 |

