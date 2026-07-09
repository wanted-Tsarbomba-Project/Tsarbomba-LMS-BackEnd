# k6 Result - chat-send-20vu

## Summary

| Metric | Value |
| --- | ---: |
| http_reqs | 692 |
| iterations | 692 |
| checks success rate | 34.20% |
| http_req_failed | 0.00% |
| data_received bytes | 482683 |
| data_sent bytes | 145151 |

## Duration Metrics

| Metric | avg(ms) | min(ms) | med(ms) | p90(ms) | p95(ms) | p99(ms) | max(ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| http_req_duration | 175.70 | 109.68 | 137.06 | 193.42 | 249.81 | 1682.62 | 3639.71 |
| http_req_waiting | 39.79 | 24.31 | 32.51 | 47.94 | 65.36 | 192.05 | 432.96 |
| http_req_blocked | 0.88 | 0.00 | 0.00 | 0.00 | 0.00 | 28.65 | 61.88 |
| http_req_connecting | 0.46 | 0 | 0 | 0 | 0 | 12.46 | 45.34 |

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
| status is 200 | 692 pass / 0 fail |
| stream completed (event:done) | 9 pass / 683 fail |
| no stream error (event:error) | 9 pass / 683 fail |

## How To Compare

| Compare Point | What To Look For |
| --- | --- |
| p95 | 사용자 대부분이 체감하는 지연 시간 악화 여부 |
| http_req_failed | 4xx/5xx 또는 check 실패 증가 여부 |
| http_req_waiting | 서버 처리나 DB 처리 지연 가능성 |
| Prometheus | 서버 내부 HTTP/custom metric 추세 |
| Loki | 느린 요청의 traceId와 event 로그 |

