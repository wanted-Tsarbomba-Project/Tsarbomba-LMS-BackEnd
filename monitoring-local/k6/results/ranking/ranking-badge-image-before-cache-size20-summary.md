# k6 Result - ranking-badge-image-before-cache-size20

## Summary

| Metric | Value |
| --- | ---: |
| http_reqs | 1487 |
| iterations | 743 |
| checks success rate | 100.00% |
| http_req_failed | 0.00% |
| data_received bytes | 30338174 |
| data_sent bytes | 525509 |

## Duration Metrics

| Metric | avg(ms) | min(ms) | med(ms) | p90(ms) | p95(ms) | p99(ms) | max(ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| http_req_duration | 1279.26 | 88.48 | 1464.65 | 1722.60 | 1786.61 | 1898.90 | 1991.42 |
| http_req_waiting | 1277.15 | 88.15 | 1462.54 | 1718.20 | 1784.71 | 1893.23 | 1990.33 |
| http_req_blocked | 0.12 | 0.00 | 0.01 | 0.01 | 0.01 | 3.44 | 6.99 |
| http_req_connecting | 0.10 | 0 | 0 | 0 | 0 | 3.19 | 6.84 |

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
| GET /rankings/points status is 200 | 743 pass / 0 fail |
| GET /rankings/points has rankings array | 743 pass / 0 fail |
| GET /rankings/points has badge image urls | 743 pass / 0 fail |
| GET /rankings/points/weekly status is 200 | 743 pass / 0 fail |
| GET /rankings/points/weekly has rankings array | 743 pass / 0 fail |
| GET /rankings/points/weekly has badge image urls | 743 pass / 0 fail |

## How To Compare

| Compare Point | What To Look For |
| --- | --- |
| p95 | 사용자 대부분이 체감하는 지연 시간 악화 여부 |
| http_req_failed | 4xx/5xx 또는 check 실패 증가 여부 |
| http_req_waiting | 서버 처리나 DB 처리 지연 가능성 |
| Prometheus | 서버 내부 HTTP/custom metric 추세 |
| Loki | 느린 요청의 traceId와 event 로그 |

