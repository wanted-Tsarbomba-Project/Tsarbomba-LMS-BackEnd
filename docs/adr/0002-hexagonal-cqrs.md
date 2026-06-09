# 도메인 구조: 헥사고날(포트/어댑터) + command/usecase 분리

## 맥락

LMS는 14개 도메인이 한 모노레포에 있다. 도메인 간 결합을 낮추고 외부 연동(JPA, FastAPI 등)을
교체 가능하게 두기 위해 도메인 내부 구조의 표준이 필요하다.

## 결정

각 도메인은 `presentation → application → domain → infrastructure` 4계층(헥사고날 포트/어댑터)으로 구성한다.
`application` 내부는 `usecase`(입력 포트) / `command` / `port`(출력 포트) / `service`(구현)로 나눠
쓰기 흐름과 외부 의존을 포트로 격리한다(부분적 CQRS).

## 근거

- `user`, `course`, `submission`, `learning`, `chatbot` 모두 4계층 + `application/{usecase,command,port,service}` 구조를 따른다.
- 포트로 격리하면 BC 간 통신(→ ADR-0003)과 infra 교체(JPA↔Mock, FastAPI↔Mock)가 쉬워진다.

## 알려진 이탈

- `problems` 도메인은 4계층이 아니라 하위 기능별(`category`, `dataset`, `set`, `execution`, `testcase` …) 구조다.
  → 의도된 선택인지 정리 대상인지 **확인 필요**. 의도된 것이면 그 이유를 본 ADR에 추가한다.
