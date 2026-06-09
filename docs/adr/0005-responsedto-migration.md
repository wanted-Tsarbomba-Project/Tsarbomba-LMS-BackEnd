# 응답 포맷 점진 마이그레이션: ResponseDTO → ApiResponse

> 상태: **완료** — 전 컨트롤러 `ApiResponse` 전환 끝. `ResponseDTO`는 `global.presentation.api.commonLegacy`에 미사용 잔존(제거 가능). 신규는 `ApiResponse`만.

## 맥락

기존 컨트롤러는 옛 `ResponseDTO`를 쓰고, 신규 표준은 `ApiResponse`(→ ADR-0004)다.
한 번에 전부 바꾸면 광범위한 회귀 위험이 있다.

## 결정

**점진 마이그레이션**한다. 신규 컨트롤러는 `ApiResponse`를 쓰고, 기존 `ResponseDTO` 컨트롤러는
유지하되 손댈 때 함께 전환한다. 두 포맷이 한동안 공존하는 것을 허용한다.

## 근거

- 모든 컨트롤러가 `ApiResponse`를 반환하고 `ResponseDTO` 참조는 0 (`commonLegacy` 잔존 클래스만 미사용).
- 응답 포맷·사용법은 `docs/convention/response.md`(살아있는 문서)가 가진다.

## Consequences

- 응답 포맷이 단일 표준(`ApiResponse`)으로 통일됨.
- 잔존 `commonLegacy.ResponseDTO`는 참조처가 없어 정리(삭제) 대상.
