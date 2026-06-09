# BC 간 통신: 포트(application service) 경유 + 도메인 이벤트, SpringData 직접 참조 금지

> 상태: **accepted** (강사 조평훈 확인 — 팀 전체 규칙).

## 맥락

모노레포라 한 도메인이 다른 도메인의 JPA 리포지토리/엔티티에 직접 손댈 수 있다. 그러면 경계가
무너지고 변경이 전파된다. 궁극 목표는 BC 단위 독립(MSA 분리 가능). BC 간 통신 방식을 정해야 한다.

## 결정

- **동기 조회/명령(데이터가 필요)**: 상대 BC의 `application.service`(포트)를 호출한다. **상대 BC의 SpringData 리포지토리·엔티티를 직접 참조하지 않는다.**
- **부수적 후속 작업**: 도메인 이벤트로 느슨하게 연결한다. (예: `submission`이 `ProblemSolvedEvent` /
  `ProblemSetCompletedEvent` 발행 → `reward`가 `PointRewardEventHandler`로 수신해 포인트 적립)
- **포트 DTO는 호출하는 BC 소유**: 포트가 정의하는 record(DTO)는 자기 모듈 것. 상대 BC의 도메인 객체를 그대로 받지 말고 자기 모듈 record로 변환한다.
- **이벤트로 조회 금지**: `publishEvent()`는 void 반환. 리턴값이 필요하면 포트.

### Port vs Event 판단 기준

순서대로 하나라도 Yes → **Port/Adapter**, 셋 다 No → **Event**.

1. 리턴값이 필요한가?
2. 이 작업이 실패하면 메인 작업도 실패해야 하는가?
3. 하나의 트랜잭션 안에서 일어나야 하는가?

## 근거

- `submission`의 `ProblemForSubmissionAdapter`가 `problems`의 `ProblemQueryService`(application service)만 호출하고 자기 모듈 DTO `ProblemForSubmission`으로 변환 — 모범 패턴.
- `chatbot`의 `ChatContextAdapter`가 상대 BC의 application service(`ProblemSetQueryService`·`SubmissionQueryService` 등)만 호출(직접 리포지토리 접근 없음).
- `submission → reward`가 이벤트로 분리되어 있어 채점 트랜잭션과 적립이 결합되지 않는다.

> 기존 코드의 규칙 위반(타 BC SpringData/JpaEntity 직접 참조 등)은 팀에 별도 전달·추적한다. 본 ADR은 결정만 기록한다.
