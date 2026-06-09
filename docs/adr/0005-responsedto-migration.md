# 응답 포맷 점진 마이그레이션: ResponseDTO → ApiResponse

## 맥락

기존 컨트롤러는 옛 `ResponseDTO`를 쓰고, 신규 표준은 `ApiResponse`(→ ADR-0004)다.
한 번에 전부 바꾸면 광범위한 회귀 위험이 있다.

## 결정

**점진 마이그레이션**한다. 신규 컨트롤러는 `ApiResponse`를 쓰고, 기존 `ResponseDTO` 컨트롤러는
유지하되 손댈 때 함께 전환한다. 두 포맷이 한동안 공존하는 것을 허용한다.

## 근거

- `docs/CONVENTION.md`가 "새 컨트롤러 — ApiResponse 사용 / 기존 컨트롤러 — ResponseDTO 유지(점진적 마이그레이션)"을 명시.

## Consequences

- 과도기 동안 응답 포맷이 혼재 → 프론트는 엔드포인트별로 확인 필요.
- 마이그레이션 완료 시점(모든 ResponseDTO 제거)에 본 ADR을 superseded로 표시한다.
