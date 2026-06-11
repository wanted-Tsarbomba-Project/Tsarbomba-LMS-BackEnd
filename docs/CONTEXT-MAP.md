# Context Map — CodeBomb LMS

모노레포에 들어있는 도메인(bounded context)들이 **무엇을 소유하고, 서로 어떻게 통신하는지**의 지도다.
각 도메인의 용어·역할 상세는 해당 도메인 `README.md`, "왜 이렇게 통신하나"의 결정은 `docs/adr/`를 본다.

> 이 파일은 **교차 도메인 경계**의 단일 소스다. 도메인별 README는 자기 경계를 여기로 참조만 한다(중복 금지).

## Contexts

| 도메인 | 소유(한 줄) |
|--------|------------|
| `auth` | 이메일 인증·회원가입·로그인·토큰 발급/재발급/무효화 |
| `user` | 사용자 계정·프로필·잠금, 타 도메인에 사용자 조회 포트 제공 |
| `course` | 강좌(생성·공개·삭제)·카테고리·강사별 강좌 |
| `lecture` | 강좌 하위 강의(생성·수정·삭제·조회) |
| `enrollment` | 수강 신청·취소·조회 |
| `problems` | 문제·문제 세트·카테고리·힌트·테스트케이스·코드 실행 |
| `submission` | 답안 제출·채점·결과 저장, 문제해결/세트완료 이벤트 발행 |
| `learning` | 학습 진행률 기록·집계·요약·통계 |
| `chatbot` | AI 채팅방·메시지, 외부 FastAPI 연동, 학습 컨텍스트 조합 |
| `reward` | 보상 이벤트 수신·포인트 적립·이력 |
| `ranking` | 전체/주간/내 포인트 랭킹 |
| `badge` | 배지 획득·장착·운영자 CRUD·이미지 업로드 |
| `admin` | 운영 알림·자동화 규칙·스케줄러 |
| `global` | 공통 인프라(JWT/Security, 공통 응답·예외, paging, 파일 업로드, scheduling) |

## Relationships (의존 방향: 호출하는 쪽 → 호출되는 쪽)

> 통신 방식 2종: **[포트]** 상대 BC의 `application.service`(포트) 동기 호출 / **[이벤트]** 도메인 이벤트 발행·구독.
> 직접 SpringData 참조로 BC를 가로지르지 않는다 → `docs/adr/0003-bc-communication.md`.

- `auth` → `user` [포트] — 계정 생성·조회·상태 검증
- `enrollment` → `course`, `user` [포트] — 강좌 상태/사용자 존재 확인
- `lecture` → `course` [포트] — 상위 강좌 유효성
- `course` → `lecture`, `problems`, `user` [포트] — 강의 연결·문제세트 유효성·권한
- `learning` → `course`, `lecture`, `problems`, `submission`, `enrollment`, `user` [포트] — 진행률 집계 원천 조회
- `chatbot` → `problems`, `submission` [포트] — AI 컨텍스트 조합 (`ChatContextAdapter`)
- `submission` → `problems` [포트] — 채점 대상 문제·테스트케이스
- `submission` → `reward` [이벤트] — `ProblemSolvedEvent` / `ProblemSetCompletedEvent` 발행 → `PointRewardEventHandler` 수신
- `submission` → `problems.set` [이벤트] — `ProblemSetCompletedEvent` → `ProblemSetCompletedEventListener`로 세트 완료 카운트 갱신
- `ranking` → `reward`, `user` [포트] — 포인트·이력 기반 랭킹 산출
- `badge` → `user`, `reward` [포트] — 사용자 배지 관리, 보상 연계
- `admin` → `user`, `problems`, `learning` [포트] — 운영 알림 대상 조회
- (전 도메인) → `global` — 공통 인프라
