# k6 Result - recommendation-generation-scale-800

## Summary

| Metric | Value |
| --- | ---: |
| http_reqs | 2 |
| iterations | 1 |
| checks success rate | 0.00% |
| http_req_failed | 50.00% |
| data_received bytes | 1140 |
| data_sent bytes | 588 |

## Duration Metrics

| Metric | avg(ms) | min(ms) | med(ms) | p90(ms) | p95(ms) | p99(ms) | max(ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| http_req_duration | 30394.92 | 795.73 | 30394.92 | 54074.26 | 57034.18 | 59402.12 | 59994.10 |
| http_req_waiting | 30393.37 | 792.76 | 30393.37 | 54073.85 | 57033.91 | 59401.96 | 59993.97 |
| http_req_blocked | 4.46 | 3.07 | 4.46 | 5.57 | 5.71 | 5.82 | 5.85 |
| http_req_connecting | 3.48 | 2.91 | 3.48 | 3.93 | 3.99 | 4.03 | 4.04 |

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
| status is 200 | 0 pass / 1 fail |
| trigger completed | 0 pass / 1 fail |

## How To Compare

| Compare Point | What To Look For |
| --- | --- |
| p95 | 사용자 대부분이 체감하는 지연 시간 악화 여부 |
| http_req_failed | 4xx/5xx 또는 check 실패 증가 여부 |
| http_req_waiting | 서버 처리나 DB 처리 지연 가능성 |
| Prometheus | 서버 내부 HTTP/custom metric 추세 |
| Loki | 느린 요청의 traceId와 event 로그 |

