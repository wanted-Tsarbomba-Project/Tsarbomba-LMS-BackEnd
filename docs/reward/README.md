# Reward Domain

`reward` 도메인은 학습 활동에 따른 포인트 지급과 포인트 이력 관리를 담당한다. 현재 구조에서는 문제 해결 이벤트를 기반으로 사용자 포인트를 적립하고, 포인트 이력을 저장하는 흐름이 중심이다.

## 주요 역할

- 문제 해결 등 보상 이벤트를 수신한다.
- 사용자 포인트를 증가시킨다.
- 포인트 지급 이력을 저장한다.
- 랭킹 산출에 필요한 포인트 데이터를 제공한다.

## 패키지 구조

```text
reward
└── point
    ├── application
    │   ├── port     # 사용자 포인트 저장 포트
    │   └── service  # 포인트 지급 서비스
    ├── domain
    │   ├── model    # UserPoint, PointHistory
    │   └── repository
    └── infrastructure
        ├── event        # 문제 해결 이벤트 핸들러
        └── persistence  # 포인트/포인트 이력 저장소
```

## 주요 모델

| 모델 | 설명 |
| --- | --- |
| `UserPoint` | 사용자별 누적 포인트 |
| `PointHistory` | 포인트 지급/변경 이력 |

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `RewardPointGrantService` | 보상 정책에 따른 포인트 지급 |
| `PointRewardEventHandler` | 도메인 이벤트를 받아 포인트 지급 서비스 호출 |

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `submission` | 문제 해결 이벤트를 발행해 포인트 지급 트리거 |
| `ranking` | 누적 포인트와 이력을 랭킹 조회에 활용 |
| `user` | 사용자별 포인트 저장 기준 |

## 참고 문서

- `api-spec.md`: reward 관련 API 또는 데이터 흐름 명세
- `clean_architecture_plan.md`: reward 도메인 클린 아키텍처 전환 계획
- `convention.md`: reward 도메인 작업 컨벤션
- `handoff.md`: 인수인계 문서
