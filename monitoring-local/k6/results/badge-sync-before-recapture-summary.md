# k6 Result - badge-sync-before-recapture

## Summary

| Metric | Value |
| --- | ---: |
| http_reqs | 3183 |
| iterations | 1591 |
| checks success rate | 100.00% |
| http_req_failed | 0.00% |
| data_received bytes | 35726055 |
| data_sent bytes | 1086861 |

## Duration Metrics

| Metric | avg(ms) | min(ms) | med(ms) | p90(ms) | p95(ms) | p99(ms) | max(ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| http_req_duration | 255.67 | 6.98 | 260.84 | 528.02 | 634.51 | 751.92 | 959.64 |
| http_req_waiting | 254.68 | 6.80 | 259.61 | 526.89 | 632.22 | 750.57 | 956.81 |
| http_req_blocked | 0.04 | 0.00 | 0.00 | 0.01 | 0.01 | 2.12 | 4.62 |
| http_req_connecting | 0.04 | 0 | 0 | 0 | 0 | 2.03 | 4.51 |

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
| badge sync status is 200 | 1591 pass / 0 fail |
| badge sync has totalPoint | 1591 pass / 0 fail |
| badge sync has newlyEarnedBadgeCount | 1591 pass / 0 fail |
| my badge status is 200 | 1591 pass / 0 fail |
| my badge has array | 1591 pass / 0 fail |

## How To Compare

| Compare Point | What To Look For |
| --- | --- |
| p95 | 사용자 대부분이 체감하는 지연 시간 악화 여부 |
| http_req_failed | 4xx/5xx 또는 check 실패 증가 여부 |
| http_req_waiting | 서버 처리나 DB 처리 지연 가능성 |
| Prometheus | 서버 내부 HTTP/custom metric 추세 |
| Loki | 느린 요청의 traceId와 event 로그 |

