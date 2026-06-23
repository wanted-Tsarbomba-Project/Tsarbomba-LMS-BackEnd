# k6 Result - recommendation-hide-before

## Summary

| Metric | Value |
| --- | ---: |
| http_reqs | 1299 |
| iterations | 1298 |
| checks success rate | 100.00% |
| http_req_failed | 0.00% |
| data_received bytes | 782176 |
| data_sent bytes | 492150 |

## Duration Metrics

| Metric | avg(ms) | min(ms) | med(ms) | p90(ms) | p95(ms) | p99(ms) | max(ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| http_req_duration | 29.40 | 14.74 | 27.59 | 37.87 | 42.98 | 63.36 | 177.32 |
| http_req_waiting | 28.65 | 14.32 | 26.87 | 37.14 | 42.34 | 62.54 | 176.52 |
| http_req_blocked | 0.14 | 0.00 | 0.01 | 0.01 | 0.02 | 5.07 | 14.92 |
| http_req_connecting | 0.12 | 0 | 0 | 0 | 0 | 4.56 | 14.66 |

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
| status is 200 | 1298 pass / 0 fail |
| hidden is true | 1298 pass / 0 fail |
| hiddenUntil exists | 1298 pass / 0 fail |

## How To Compare

| Compare Point | What To Look For |
| --- | --- |
| p95 | 사용자 대부분이 체감하는 지연 시간 악화 여부 |
| http_req_failed | 4xx/5xx 또는 check 실패 증가 여부 |
| http_req_waiting | 서버 처리나 DB 처리 지연 가능성 |
| Prometheus | 서버 내부 HTTP/custom metric 추세 |
| Loki | 느린 요청의 traceId와 event 로그 |

