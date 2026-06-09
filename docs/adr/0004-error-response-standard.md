# 에러코드·응답 표준: {DOMAIN}-{NNN} + 글로벌 ApiResponse/ApiErrorResponse

## 맥락

14개 도메인이 제각각 응답·에러 포맷을 쓰면 프론트·팀원이 매번 다른 규칙을 익혀야 한다.
공통 응답/에러 계약이 필요하다.

## 결정

- 에러코드는 `{DOMAIN}-{NNN}` (하위도메인 있으면 `{DOMAIN}-{SUBDOMAIN}-{NNN}`) 포맷으로 통일한다.
- 성공 응답은 `global.presentation.api.common.ApiResponse`, 에러 응답은 `ApiErrorResponse`,
  코드는 `ApiResponseCode`로 표준화한다.

## 근거

- `global/presentation/api/common/` 에 `ApiResponse`·`ApiResponseCode`가 실재하고, 에러코드 표/예외 타입/응답 포맷이 `docs/CONVENTION.md`에 정의돼 있다.

## 비고

이 ADR은 **결정과 이유**만 박제한다. 에러코드 추가법·파일 위치·Swagger 예시 등 **자주 바뀌는 how-to는 `docs/CONVENTION.md`(살아있는 문서)** 가 계속 가진다. ADR은 덮어쓰지 않고, 표준이 바뀌면 새 ADR로 supersede 한다.
