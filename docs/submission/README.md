# Submission Domain

`submission` 도메인은 문제 답안 제출과 코드 제출 채점 결과 관리를 담당한다. 텍스트 답안 채점, 코드 테스트 케이스 채점, 제출 이력 조회, 문제별 오답률 조회, 문제 해결 이벤트 발행이 포함된다.

## 주요 역할

- 문제 답안을 제출하고 채점한다.
- 코드 문제 제출을 테스트 케이스 기준으로 채점한다.
- 제출 결과와 테스트 케이스별 결과를 저장한다.
- 최신 제출과 코드 제출 목록을 조회한다.
- 문제별 제출 지표를 조회한다.
- 정답 처리 시 문제 해결과 문제 세트 완료 이벤트를 발행한다.

## 패키지 구조

```text
submission
├── application
│   ├── command      # 제출 명령
│   ├── policy       # 제출 횟수, 코드 정책
│   ├── port         # 문제, 테스트 케이스, 진행률, 이벤트 연동 포트
│   ├── service      # 제출/채점/조회 서비스
│   └── usecase      # 입력 포트
├── domain
│   ├── event        # ProblemSolvedEvent, ProblemSetCompletedEvent
│   └── model        # 제출과 채점 결과 모델
├── exception
├── infrastructure
│   ├── event
│   ├── persistence
│   ├── problemset
│   ├── progress
│   └── testcase
└── presentation     # SubmissionController, request/response DTO
```

## 주요 모델

| 모델 | 설명 |
| --- | --- |
| `TextSubmission` | 일반 답안 제출 |
| `CodeSubmission` | 코드 문제 제출 |
| `SubmissionTestResult` | 테스트 케이스별 채점 결과 |
| `CodeSubmissionResult` | 코드 제출 최종 결과 |
| `LatestSubmission` | 최신 제출 정보 |
| `CodeSubmissionPage` | 코드 제출 목록 페이지 |

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `SubmissionService` | 문제 제출 명령 처리 |
| `SubmissionQueryService` | 제출 조회 |
| `AnswerGradingService` | 일반 답안 채점 |
| `CodeGradingService` | 코드 제출 테스트 케이스 채점 |
| `CodeSubmissionResultQueryService` | 코드 제출 결과 상세 조회 |
| `CodeSubmissionListQueryService` | 코드 제출 목록 조회 |
| `ProblemSubmissionMetricQueryService` | 문제별 제출 지표 조회 |

## API 목록

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/v1/problems/{problemId}/submissions` | 문제 답안 제출 |
| `GET` | `/api/v1/problems/{problemId}/submissions` | 문제 제출 조회 |
| `GET` | `/api/v1/code-problems/{problemId}/submissions` | 코드 문제 제출 목록 조회 |

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `problems` | 제출 대상 문제와 테스트 케이스 조회 |
| `learning` | 문제 진행률 갱신 |
| `reward` | 문제 해결 이벤트를 통한 포인트 지급 |
| `ranking` | 포인트 변화가 랭킹에 반영 |

## 참고 문서

- `api-spec.md`: submission API 상세 명세
- `clean_architecture_plan.md`: submission 도메인 클린 아키텍처 전환 계획
- `convention.md`: submission 도메인 작업 컨벤션
- `handoff.md`: 인수인계 문서
