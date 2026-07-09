# Reward Domain

`reward` 도메인은 학습 활동에 따른 포인트 지급과 포인트 이력 관리를 담당한다. 현재 구조에서는 문제 해결 이벤트를 기반으로 사용자 포인트를 적립하고, 포인트 이력을 저장하는 흐름이 중심이다.

## 주요 역할

- 문제 해결 등 보상 이벤트를 수신한다.
- 사용자 포인트를 증가시킨다.
- 포인트 지급 이력을 저장한다.
- 포인트 지급 작업을 예약하고, 실패 시 재시도할 수 있는 작업 상태를 관리한다.
- 제출 저장 트랜잭션과 분리된 독립 트랜잭션에서 보상 작업을 예약/처리한다.
- 랭킹 산출에 필요한 포인트 데이터를 제공한다.

## 패키지 구조

```text
reward
└── point
    ├── application
    │   ├── port     # 사용자 포인트 저장, 메트릭 기록 포트
    │   ├── usecase  # 포인트 지급 작업 예약/처리 유스케이스
    │   └── service  # 포인트 지급 및 작업 처리 서비스
    ├── domain
    │   ├── model    # UserPoint, PointHistory, PointRewardTask
    │   └── repository
    └── infrastructure
        ├── event        # 문제 해결 이벤트 핸들러
        ├── metrics      # Reward 작업 메트릭
        ├── scheduler    # pending 작업 복구 스케줄러
        └── persistence  # 포인트/포인트 이력/포인트 작업 저장소
```

## 주요 모델

| 모델 | 설명 |
| --- | --- |
| `UserPoint` | 사용자별 누적 포인트 |
| `PointHistory` | 포인트 지급/변경 이력 |
| `PointRewardTask` | 문제 정답 제출 후 포인트 지급을 예약/처리하는 작업 |

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `RewardPointGrantService` | 보상 정책에 따른 포인트 지급 |
| `PointRewardTaskService` | 포인트 지급 작업 예약, 즉시 처리, 재시도 상태 관리 |
| `PointRewardEventHandler` | 문제 해결 이벤트를 받아 커밋 이후 포인트 지급 작업 예약/처리 |

## 보상 작업 처리 정책

Submission 도메인은 정답 제출 저장과 `ProblemSolvedEvent` 발행까지만 담당한다.  
Reward 도메인은 제출 트랜잭션이 커밋된 뒤(`AFTER_COMMIT`) 포인트 지급 작업을 예약하고 처리한다.

```text
Submission 정답 저장
→ ProblemSolvedEvent 발행
→ Submission 트랜잭션 commit
→ Reward AFTER_COMMIT listener 실행
→ point_reward_task 예약
→ 포인트 지급 처리
```

`point_reward_task(user_id, problem_id)` 중복 예약이 발생하면 Submission 실패로 전파하지 않고 이미 예약된 작업으로 보고 skip 로그를 남긴다.

```text
event=reward_point_task_schedule_skipped reason=already_scheduled
```

보상 작업 예약과 처리 메서드는 `REQUIRES_NEW` 트랜잭션으로 실행해 제출 저장 트랜잭션과 영향을 분리한다.

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `submission` | 문제 해결 이벤트를 발행해 포인트 지급 작업 예약 트리거 |
| `ranking` | 누적 포인트와 이력을 랭킹 조회에 활용 |
| `user` | 사용자별 포인트 저장 기준 |

