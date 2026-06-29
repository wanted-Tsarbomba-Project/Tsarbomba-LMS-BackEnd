# k6 Result - badge-sync-before

## Summary

| Metric | Value |
| --- | ---: |
| http_reqs | 1423 |
| iterations | 711 |
| checks success rate | 99.72% |
| http_req_failed | 0.42% |
| data_received bytes | 15882465 |
| data_sent bytes | 501463 |

## Duration Metrics

| Metric | avg(ms) | min(ms) | med(ms) | p90(ms) | p95(ms) | p99(ms) | max(ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| http_req_duration | 1373.91 | 10.31 | 1374.38 | 2394.53 | 2614.83 | 3008.04 | 3261.38 |
| http_req_waiting | 1373.02 | 10.08 | 1374.19 | 2393.54 | 2614.31 | 3007.12 | 3260.28 |
| http_req_blocked | 0.10 | 0.00 | 0.00 | 0.01 | 0.01 | 2.74 | 3.91 |
| http_req_connecting | 0.09 | 0 | 0 | 0 | 0 | 2.60 | 3.12 |

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
| badge sync status is 200 | 709 pass / 2 fail |
| badge sync has totalPoint | 709 pass / 2 fail |
| badge sync has newlyEarnedBadgeCount | 709 pass / 2 fail |
| my badge status is 200 | 707 pass / 4 fail |
| my badge has array | 711 pass / 0 fail |

## How To Compare

| Compare Point | What To Look For |
| --- | --- |
| p95 | 사용자 대부분이 체감하는 지연 시간 악화 여부 |
| http_req_failed | 4xx/5xx 또는 check 실패 증가 여부 |
| http_req_waiting | 서버 처리나 DB 처리 지연 가능성 |
| Prometheus | 서버 내부 HTTP/custom metric 추세 |
| Loki | 느린 요청의 traceId와 event 로그 |

