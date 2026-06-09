# AI 협업 문서: 공용은 커밋·PL 소유, 개인 작업메모는 ignore

## 맥락

14인 모노레포(LMS)에 1인 개발자용 4종 파일 체계(AGENTS / STATE / API / WORKLOG)를 얹으면서
"개인 것 vs 팀 공유 것"의 경계가 없어 충돌이 생겼다. 직전까지는 `.ai/` 전체와 `AGENTS.md` /
`CLAUDE.md` / `CONTEXT.md` 를 `.gitignore` 로 막아 두어, 정작 팀이 공유해야 할 문서를 팀원·팀원 AI가
볼 수 없는 상태였다.

## 결정

- **공용 문서**(`AGENTS.md`, `CLAUDE.md`, `CONTEXT-MAP.md`, `docs/adr/`, `docs/CONVENTION.md`,
  `docs/PR-COMMIT.md`)는 **커밋**하고 PL(@suerovr)이 소유·머지한다(`.github/CODEOWNERS`).
- **개인 작업메모**(`STATE.md`·`API.md`·`WORKLOG.md` 등 도메인 개발하며 만든 라이브 노트 = 세컨드브레인)는
  `.ai/local/` 폴더에 두고 **통째로 git-ignore** 한다. 사람마다 다르고 자주 바뀌므로 공유하지 않는다.
- `AGENTS.md`는 **도메인 중립**으로 유지한다(개인 담당 도메인·마일스톤은 STATE로). 길었던 응답 양식은
  분리한다 — **팀 합의 양식(PR·커밋)은 `docs/PR-COMMIT.md`(공용), 개인 응답 취향(톤·이모지 등)은 `.ai/local/`(개인).**
- PL 본인의 크로스머신(Windows/macOS) 개인메모는 레포 밖 `~/.claude` 메모리를 쓴다.
  (`.ai/local/` 은 ignore라 머신 간 git 동기화가 안 되므로.)

## 이유

- git-ignore로 전부 막으면 "팀 전체 동일 문서" 목적과 정면충돌한다.
- 개인 라이브 상태(STATE)를 한 파일로 공유하면 14명 머지 지옥 → 개인화·ignore가 맞다.
- 공용 표준 문서는 오염·머지충돌 방지를 위해 ignore가 아니라 **CODEOWNERS로 소유권을 분리**한다
  (막는 게 아니라 문지기를 둔다).

## 비고

- "PR 승인 없이 머지 차단"은 GitHub의 Branch protection > "Require review from Code Owners"를
  켜야 강제된다(웹 UI 설정, 코드로 못 함).
