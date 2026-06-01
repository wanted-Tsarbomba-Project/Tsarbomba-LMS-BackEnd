# Ranking Domain

`ranking` 도메인은 포인트 기반 사용자 랭킹을 조회한다. 전체 랭킹, 주간 랭킹, 내 랭킹 정보를 제공하며 포인트 데이터는 보상/사용자 정보와 연결된다.

## 주요 역할

- 전체 포인트 랭킹을 조회한다.
- 주간 포인트 랭킹을 조회한다.
- 로그인 사용자의 현재 랭킹을 조회한다.

## 패키지 구조

```text
ranking
├── application
│   ├── port         # 랭킹 조회 포트
│   ├── query        # 랭킹 조회 결과 모델
│   ├── service      # RankingService
│   └── usecase      # 랭킹 조회 입력 포트
├── exception
├── infrastructure
│   └── persistence  # 랭킹 query adapter
└── presentation     # RankingController, response DTO
```

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `RankingService` | 전체/주간/내 포인트 랭킹 조회 |

## API 목록

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/api/v1/rankings/points` | 전체 포인트 랭킹 조회 |
| `GET` | `/api/v1/rankings/points/weekly` | 주간 포인트 랭킹 조회 |
| `GET` | `/api/v1/rankings/points/me` | 내 포인트 랭킹 조회 |

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `reward` | 사용자 포인트와 포인트 이력 기준 랭킹 산출 |
| `user` | 랭킹에 표시할 사용자 정보 조회 |

