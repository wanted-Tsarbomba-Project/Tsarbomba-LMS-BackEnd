# Learning Domain

`learning` 도메인은 강의와 문제 세트 학습 진행률을 기록하고 조회한다. 학생 개인의 학습 상태뿐 아니라 강좌/강의 단위의 관리자용 진행률과 문제 통계도 제공한다.

## 주요 역할

- 강의 시청 또는 학습 진행률을 기록한다.
- 강의별 진행률을 조회한다.
- 강의 문제 세트 상세와 문제별 진행률을 조회한다.
- 문제 제출을 학습 흐름에서 처리한다.
- 강좌별, 사용자별, 강의별 학습 진행률을 집계한다.
- 학습 진행률 요약과 문제 통계를 제공한다.

## 패키지 구조

```text
learning
├── application
│   ├── command      # 진행률 기록 명령
│   ├── port         # course, lecture, problem, enrollment, user 연동 포트
│   ├── service      # 진행률 명령/조회 서비스
│   └── usecase      # 입력 포트
├── domain
│   ├── exception
│   └── model        # 진행률과 통계 모델
├── infrastructure
│   ├── course
│   ├── enrollment
│   ├── lecture
│   ├── persistence
│   ├── problem
│   └── user
└── presentation
    └── api          # LearningController, request/response DTO
```

## 주요 모델

| 모델 | 설명 |
| --- | --- |
| `LectureProgress` | 강의 단위 학습 진행률 |
| `LectureProblemProgress` | 강의 문제별 풀이 진행률 |
| `LectureProblemStatistics` | 강의 문제 통계 |
| `CourseLearningProgress` | 강좌 단위 학습 진행률 |
| `StudentLearningProgress` | 학생별 학습 진행률 |
| `LearningProgressSummary` | 전체 학습 진행률 요약 |

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `LectureProgressService` | 강의 진행률 기록/조회 |
| `LectureProblemProgressService` | 강의 문제 진행률 기록/조회 |
| `LectureProblemSetService` | 강의 문제 세트 상세 조회와 제출 흐름 |
| `AdminLearningProgressQueryService` | 관리자용 학습 진행률 집계 조회 |

## API 목록

| Method | Path | 설명 |
| --- | --- | --- |
| `PATCH` | `/api/v1/lectures/{lectureId}/progress` | 강의 진행률 기록 |
| `GET` | `/api/v1/lectures/{lectureId}/progress` | 강의 진행률 조회 |
| `GET` | `/api/v1/lecture-problem-sets/{lectureProblemSetId}` | 강의 문제 세트 상세 조회 |
| `GET` | `/api/v1/lecture-problem-sets/{lectureProblemSetId}/progress` | 강의 문제 세트 진행률 조회 |
| `PATCH` | `/api/v1/lecture-problem-sets/{lectureProblemSetId}/progress` | 강의 문제 진행률 기록 |
| `POST` | `/api/v1/lecture-problem-sets/{lectureProblemSetId}/problems/{problemId}/submissions` | 학습 흐름 내 문제 제출 |
| `GET` | `/api/v1/courses/{courseId}/learning-progress` | 내 강좌 학습 진행률 조회 |
| `GET` | `/api/v1/courses/learning-progress` | 내 전체 강좌 학습 진행률 조회 |
| `GET` | `/api/v1/courses/{courseId}/users/learning-progress` | 강좌 사용자별 학습 진행률 조회 |
| `GET` | `/api/v1/courses/{courseId}/users/{userId}/learning-progress` | 특정 사용자 강좌 학습 진행률 조회 |
| `GET` | `/api/v1/courses/{courseId}/lectures/learning-progress` | 강좌 강의별 학습 진행률 조회 |
| `GET` | `/api/v1/lectures/{lectureId}/problems/statistics` | 강의 문제 통계 조회 |
| `GET` | `/api/v1/learning-progress/summary` | 학습 진행률 요약 조회 |

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `course` | 강좌와 강좌별 문제 세트 기준 조회 |
| `lecture` | 강의 진행률 기록과 강의 존재 여부 확인 |
| `problems` | 문제 세트, 문제, 제출 대상 정보 조회 |
| `submission` | 문제 제출 결과와 풀이 상태 반영 |
| `enrollment` | 수강 중인 사용자/강좌 기준 검증 |
| `user` | 사용자별 학습 진행률 집계 |

