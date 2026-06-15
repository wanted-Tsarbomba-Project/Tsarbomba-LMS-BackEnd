# Lecture Domain

`lecture` 도메인은 강좌에 포함되는 강의 단위와 강의 문제세트 연결을 관리한다. 강좌별 강의 목록, 강의 상세, 강의 생성/수정/삭제, MAIN/FINAL 문제세트 연결 기능을 제공한다.

## 주요 역할

- 특정 강좌의 강의 목록을 조회한다.
- 강의 상세 정보를 조회한다.
- 운영자가 강좌에 강의를 생성한다.
- 운영자가 강의를 수정하거나 삭제한다.
- 강의와 문제세트의 연결 및 `MAIN`/`FINAL` 역할 정책을 관리한다.
- 강좌 삭제 시 연동되어 하위 강의를 삭제 처리한다.

## 패키지 구조

```text
lecture
├── application
│   ├── command      # 강의 생성/수정, 문제세트 연결 명령
│   ├── port         # course, problems 도메인 연동 포트
│   ├── service      # 강의 및 문제세트 연결 명령/조회 서비스
│   └── usecase      # 입력 포트
├── domain
│   ├── exception
│   ├── model        # Lecture, LectureStatus, LectureProblemSet
│   └── repository
├── infrastructure
│   ├── cleanup      # 강의 hard delete 대상
│   ├── course       # course 도메인 어댑터
│   ├── problem      # problems 도메인 어댑터
│   └── persistence
└── presentation
    └── api          # LectureController, request/response DTO
```

## 주요 모델

| 모델 | 설명 |
| --- | --- |
| `Lecture` | 강좌에 소속된 강의 정보 |
| `LectureStatus` | 강의 상태 |
| `LectureProblemSet` | 강좌·강의·문제세트 연결과 MAIN/FINAL 역할, 노출 순서 |
| `LectureProblemSetRole` | `MAIN`, `FINAL` 역할 구분 |

## 주요 서비스

| 서비스 | 책임 |
| --- | --- |
| `LectureCommandService` | 강의 생성, 수정, 삭제 |
| `LectureQueryService` | 강좌별 강의 목록과 강의 상세 조회 |
| `LectureProblemSetCommandService` | 강의 문제세트 연결 저장과 역할 정책 검증 |
| `LectureProblemSetQueryService` | 강좌·강의·연결 ID 기준 문제세트 연결 조회 |

## API 목록

| Method | Path | 설명 | 권한 |
| --- | --- | --- | --- |
| `GET` | `/api/v1/courses/{courseId}/lectures` | 강좌별 강의 목록 조회 | 전체 |
| `GET` | `/api/v1/lectures/{lectureId}` | 강의 상세 조회 | 전체 |
| `POST` | `/api/v1/courses/{courseId}/lectures` | 강의 생성 | 운영자 |
| `PUT` | `/api/v1/lectures/{lectureId}` | 강의 수정 | 운영자 |
| `DELETE` | `/api/v1/lectures/{lectureId}` | 강의 삭제 | 운영자 |

## 다른 도메인과의 연동

| 대상 도메인 | 연동 내용 |
| --- | --- |
| `course` | 강의가 속할 강좌의 존재 여부와 상태 확인 |
| `learning` | 강의 진행률 기록과 조회의 기준 |
| `problems` | 강의에 연결된 문제 세트 조회 기준 |
