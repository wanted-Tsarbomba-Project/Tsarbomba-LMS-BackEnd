# Admin Domain

`admin` 도메인은 운영자가 LMS 상태를 모니터링하고 자동화 규칙과 운영 알림을 관리하는 기능을 담당한다. 현재 구조는 운영 알림(`operation alert`)과 자동화 규칙(`automation rule`)을 중심으로 구성되어 있다.

## 주요 역할

- 운영 알림 목록과 상세 정보를 조회한다.
- 운영 알림의 메모와 상태를 수정한다.
- 운영 알림을 삭제한다.
- 자동화 규칙 목록을 조회한다.
- 자동화 규칙을 수정한다.
- 자동화 규칙 활성화 여부를 변경한다.
- 스케줄러를 통해 규칙을 실행하고 운영 알림을 생성한다.

## 패키지 구조

```text
admin
└── operation
    ├── alert
    │   ├── application     # 운영 알림 명령/조회 유스케이스
    │   ├── domain          # OperationAlert, OperationAlertStatus
    │   ├── infrastructure  # persistence, cleanup, target detail adapter
    │   └── presentation    # OperationAlertController
    ├── automation
    │   └── infrastructure  # OperationRuleScheduler
    ├── common
    │   ├── application     # PageResult
    │   └── domain          # OperationSeverity, OperationTargetType
    └── rule
        ├── application     # 자동화 규칙 명령/조회/실행 서비스
        ├── domain          # AutomationRule, OperationRuleCode
        ├── infrastructure  # persistence adapter
        └── presentation    # AutomationRuleController
```

## 주요 모델

| 모델 | 설명 |
| --- | --- |
| `OperationAlert` | 운영자가 확인해야 하는 이상 징후 또는 관리 대상 알림 |
| `OperationAlertStatus` | 운영 알림 처리 상태 |
| `AutomationRule` | 자동으로 운영 알림을 만들기 위한 규칙 |
| `OperationRuleCode` | 자동화 규칙 종류 |
| `OperationSeverity` | 운영 알림 심각도 |
| `OperationTargetType` | 운영 알림 대상 타입 |

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `OperationAlertQueryService` | 운영 알림 목록/상세 조회 |
| `OperationAlertCommandService` | 운영 알림 메모/상태 수정, 삭제 |
| `AutomationRuleQueryService` | 자동화 규칙 목록 조회 |
| `AutomationRuleCommandService` | 자동화 규칙 수정/활성화 변경 |
| `OperationRuleExecutionService` | 자동화 규칙 실행과 운영 알림 생성 |

## API 목록

| Method | Path | 설명 | 권한 |
| --- | --- | --- | --- |
| `GET` | `/api/v1/admin/operation-alerts` | 운영 알림 목록 조회 | 운영자 |
| `GET` | `/api/v1/admin/operation-alerts/{operationAlertId}` | 운영 알림 상세 조회 | 운영자 |
| `PATCH` | `/api/v1/admin/operation-alerts/{operationAlertId}/memo` | 운영 알림 메모 수정 | 운영자 |
| `PATCH` | `/api/v1/admin/operation-alerts/{operationAlertId}/status` | 운영 알림 상태 수정 | 운영자 |
| `DELETE` | `/api/v1/admin/operation-alerts/{operationAlertId}` | 운영 알림 삭제 | 운영자 |
| `GET` | `/api/v1/admin/automation-rules` | 자동화 규칙 목록 조회 | 운영자 |
| `PATCH` | `/api/v1/admin/automation-rules` | 자동화 규칙 수정 | 운영자 |
| `PATCH` | `/api/v1/admin/automation-rules/{automationRuleId}/enabled` | 자동화 규칙 활성화 변경 | 운영자 |

## 핵심 흐름

### 운영 알림 조회

1. 운영자가 목록 또는 상세 API를 호출한다.
2. query service가 필터, 페이징, 대상 정보를 조합해 알림을 조회한다.
3. 대상 상세 정보는 target type에 맞는 adapter를 통해 보강된다.

### 자동화 규칙 실행

1. `OperationRuleScheduler`가 활성화된 자동화 규칙을 주기적으로 실행한다.
2. `OperationRuleExecutionService`가 규칙 조건을 평가한다.
3. 조건에 맞는 대상이 있으면 `OperationAlert`를 생성한다.

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `user` | 운영 알림 대상 또는 담당자 정보 조회 |
| `problems` | 문제/문제 세트 관련 운영 알림 대상 상세 조회 |
| `learning` | 학습 진행률 기반 이상 징후 판단에 활용 가능 |
| `global` | 공통 paging, cleanup, 예외, scheduling 설정 사용 |
