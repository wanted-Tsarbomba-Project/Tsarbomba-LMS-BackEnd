# Problems Domain

`problems` 도메인은 문제 세트, 문제, 카테고리, 테스트 케이스, 힌트, 실행, 진행률, 결과, 데이터셋을 관리하는 문제 풀이 핵심 도메인이다. 강좌와 학습 도메인에서 사용하는 문제 콘텐츠의 생성, 조회, 실행, 결과 확인 흐름을 담당한다.

## 주요 역할

- 문제 카테고리 목록을 조회한다.
- 문제 세트 목록과 상세 정보를 조회한다.
- 운영자가 문제 세트와 문제를 생성, 수정, 삭제한다.
- 데이터셋 기반 문제 세트를 등록하거나 수정한다.
- 문제 힌트와 테스트 케이스를 관리한다.
- 코드 문제 실행을 요청하고 결과를 반환한다.
- 문제 세트 진행률과 결과를 조회한다.

## 패키지 구조

```text
problems
├── category     # 문제 카테고리 조회
├── dataset      # 문제 데이터셋 업로드/조회
├── execution    # 코드 실행 요청과 실행 클라이언트
├── hint         # 문제 힌트 조회/저장
├── problem      # 개별 문제 조회와 대상 상세 조회
├── progress     # 문제 세트 진행률 조회/갱신
├── result       # 문제 세트 결과 조회
├── set          # 문제 세트 생성/수정/삭제/입장/목록 조회
└── testcase     # 문제 테스트 케이스 생성/수정/삭제/조회
```

## 주요 하위 모듈

| 모듈 | 설명 |
| --- | --- |
| `category` | 문제 카테고리 목록과 상태 관리 |
| `dataset` | 데이터셋 파일 저장과 데이터셋 기반 문제 구성 |
| `execution` | 코드 실행 요청을 외부 실행 환경 또는 Mock runner로 전달 |
| `hint` | 문제별 힌트 조회 |
| `problem` | 개별 문제 정보와 운영 알림 대상 상세 조회 |
| `progress` | 사용자별 문제 세트 진행 상태 조회 |
| `result` | 문제 세트 완료 결과와 문제별 제출 결과 조회 |
| `set` | 문제 세트 등록, 수정, 삭제, 입장, 목록 조회 |
| `testcase` | 코드 문제 테스트 케이스 관리 |

## 주요 모델

| 모델 | 설명 |
| --- | --- |
| `ProblemCategory` | 문제 카테고리 |
| `ProblemDataset` | 데이터셋 기반 문제 생성에 사용하는 파일/데이터 정보 |
| `Problem` | 개별 문제 |
| `ProblemHint` | 문제 힌트 |
| `ProblemProgress` | 문제 세트 진행률 |
| `ProblemSetResult` | 문제 세트 풀이 결과 |
| `ProblemSetSummary` | 문제 세트 목록 요약 |
| `ProblemSetEntry` | 문제 세트 입장 시 내려주는 상세 구성 |
| `ProblemTestCase` | 코드 문제 테스트 케이스 |

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `ProblemCategoryQueryService` | 문제 카테고리 조회 |
| `ProblemDatasetCommandService` | 문제 데이터셋 업로드/등록 처리 |
| `ProblemDatasetQueryService` | 문제 데이터셋 조회 |
| `CodeExecutionService` | 코드 실행 요청 처리 |
| `ProblemHintService` | 문제 힌트 조회 |
| `ProblemQueryService` | 개별 문제 조회 |
| `ProblemProgressQueryService` | 문제 세트 진행률 조회 |
| `ProgressCommandService` | 문제 진행 상태 갱신 |
| `ProblemSetResultQueryService` | 문제 세트 결과 조회 |
| `ProblemSetRegistrationService` | 문제 세트 등록 |
| `ProblemSetWithDatasetRegistrationService` | 데이터셋 기반 문제 세트 등록 |
| `ProblemSetUpdateService` | 문제 세트 수정 |
| `ProblemSetWithDatasetUpdateService` | 데이터셋 기반 문제 세트 수정 |
| `ProblemSetDeleteService` | 문제 세트 삭제 |
| `ProblemSetEntryService` | 문제 세트 입장 정보 조회 |
| `ProblemTestCaseCommandService` | 테스트 케이스 생성/수정/삭제 |
| `ProblemTestCaseQueryService` | 테스트 케이스 목록 조회 |

## API 목록

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/api/v1/problem-categories` | 문제 카테고리 목록 조회 |
| `GET` | `/api/v1/problem-sets` | 문제 세트 목록 조회 |
| `GET` | `/api/v1/problem-sets/{problemSetId}` | 문제 세트 상세 조회 |
| `POST` | `/api/v1/problems` | 문제 세트 생성 |
| `PUT` | `/api/v1/problems/{problemSetId}` | 문제 세트 수정 |
| `DELETE` | `/api/v1/problems/{problemSetId}` | 문제 세트 삭제 |
| `GET` | `/api/v1/problems/{problemSetId}` | 문제 세트 관리용 상세 조회 |
| `GET` | `/api/v1/problems/{problemId}/hints` | 문제 힌트 조회 |
| `POST` | `/api/v1/problems/{problemId}/test-cases` | 테스트 케이스 생성 |
| `GET` | `/api/v1/problems/{problemId}/test-cases` | 테스트 케이스 목록 조회 |
| `PUT` | `/api/v1/test-cases/{testCaseId}` | 테스트 케이스 수정 |
| `DELETE` | `/api/v1/test-cases/{testCaseId}` | 테스트 케이스 삭제 |
| `POST` | `/api/v1/code-problems/{problemId}/executions` | 코드 실행 |
| `GET` | `/api/v1/problem-sets/{problemSetId}/progress` | 문제 세트 진행률 조회 |
| `GET` | `/api/v1/problem-sets/{problemSetId}/result` | 문제 세트 결과 조회 |

## 핵심 흐름

### 문제 세트 등록

1. 운영자가 문제 세트와 문제 목록을 전달한다.
2. 카테고리, 문제 구성, 힌트, 시작 코드, 테스트 케이스 등의 입력값을 검증한다.
3. 문제 세트와 하위 문제를 저장한다.
4. 데이터셋 기반 등록인 경우 데이터셋 파일을 파싱해 문제 구성에 반영한다.

### 문제 세트 입장

1. 학습자가 문제 세트 상세 또는 입장 API를 호출한다.
2. 문제 세트 기본 정보와 문제 목록을 조회한다.
3. 사용자 진행률이 없으면 초기 진행 상태를 생성한다.
4. 문제, 힌트, 시작 코드, 진행 상태를 응답으로 구성한다.

### 코드 실행

1. 사용자가 문제 ID와 코드를 전달한다.
2. 문제 존재 여부와 실행 가능한 문제인지 확인한다.
3. `CodeRunner` 어댑터가 외부 실행 환경 또는 Mock runner에 실행을 요청한다.
4. 실행 결과를 표준 응답으로 반환한다.

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `course` | 강좌/강의에 연결되는 문제 세트 제공 |
| `learning` | 문제 세트 진행률과 문제별 학습 상태 제공 |
| `submission` | 제출 대상 문제와 테스트 케이스 제공 |
| `chatbot` | AI 학습 컨텍스트에 문제 세트와 문제 정보 제공 |
| `admin` | 운영 알림 대상 상세 정보 제공 |
| `reward` | 문제 해결 이벤트의 원천 데이터 제공 |
