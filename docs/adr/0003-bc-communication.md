# BC 간 통신: 포트(application service) 경유 + 도메인 이벤트, SpringData 직접 참조 금지

## 맥락

모노레포라 한 도메인이 다른 도메인의 JPA 리포지토리/엔티티에 직접 손댈 수 있다. 그러면 경계가
무너지고 변경이 전파된다. BC 간 통신 방식을 정해야 한다.

## 결정

- **동기 조회/명령**: 상대 BC의 `application.service`(포트)를 호출한다. **상대 BC의 SpringData 리포지토리·엔티티를 직접 참조하지 않는다.**
- **부수효과 전파**: 도메인 이벤트로 느슨하게 연결한다. (예: `submission`이 `ProblemSolvedEvent` /
  `ProblemSetCompletedEvent` 발행 → `reward`가 `PointRewardEventHandler`로 수신해 포인트 적립)

## 근거

- `chatbot`의 `ChatContextAdapter`가 `ProblemSetQueryService`·`SubmissionQueryService` 등 상대 BC의 application service만 호출(직접 리포지토리 접근 없음).
- `submission → reward`가 이벤트로 분리되어 있어 채점 트랜잭션과 적립이 결합되지 않는다.

## 검토 대상

- ⚠️ **팀 전체 규칙 여부 확인 필요**: 현재 근거는 chatbot/submission 등에서 확인했으나, 14개 도메인 전부가 이 규칙을 지키는지(예외 없는지) 점검 후 "accepted"로 확정한다.
- **경계 누수**: `chatbot`이 포트는 경유하나 반환 타입으로 `submission.domain.model.LatestSubmission`(상대 BC 도메인 모델)을 직접 import. 포트 반환 타입을 자체 DTO로 막을지 검토.
