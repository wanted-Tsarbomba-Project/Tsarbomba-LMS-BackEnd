# 챗봇 메시지 전송 부하테스트 — codebomba 경로 (monitoring-deploy)

> **목적**: "N명이 동시에 챗봇 메시지를 보내는" 상황을 **실제 사용자 경로(`https://codebomba.com`)** 로 재현해서, 프록시 → Spring → FastAPI(LLM)까지 전 구간의 동시성 한계를 본다.
> **스크립트**: [`k6/scripts/chat/02-send-message-baseline.js`](../k6/scripts/chat/02-send-message-baseline.js)
> **대상 엔드포인트**: `POST /api/v1/chat/messages` (첫 메시지 → 방 자동생성 + SSE 스트리밍)

---

## ⚠️ 0. 먼저 읽어라 — 이건 "쓰기 + 과금" 테스트다

`monitoring-deploy`는 원래 **읽기(GET) 전용** 키트다(실 RDS·실 서비스 보호 목적). 이 시나리오는 예외적으로 **쓰기/생성 + 외부 LLM 호출**이라, 쏘면:

- **실 RDS(codebomba)에 채팅방·메시지가 실제로 쌓인다** — 20 VU × ~55초 = 수백 건. 끝나고 정리 고려.
- **실 LLM 토큰이 소모된다** — 매 요청 = 실제 과금.
- **SSE는 무겁다** — 각 스트림이 AI 생성 끝까지 커넥션을 점유. 같은 VU라도 GET보다 훨씬 부담.

→ **의도적 1회성 테스트로만**. 무인 반복·고VU 금지.

---

## 1. codebomba 경로 vs Spring 직격(IP:8080)

| | `https://codebomba.com` (권장) | `http://43.200.241.157:8080` |
|---|---|---|
| 경로 | DNS → TLS → **프록시/ALB** → Spring | Spring **직격** |
| 측정 대상 | 사용자 체감 end-to-end | 순수 앱/DB 병목 |
| SSE 검증 | **프록시 버퍼링까지** 검증됨 | 프록시 이슈 못 잡음 |
| 언제 | 실제 사용자 경험·프록시 포함 병목 | 앱/DB만 격리 |

> 챗봇은 **SSE 스트리밍**이라 리버스 프록시(nginx)가 버퍼링/끊김을 일으키기 쉽다. 그래서 **codebomba 경로 권장**. (컨트롤러의 `X-Accel-Buffering: no` 헤더가 프록시 존재 방증)
> 관측(배포 ③ Grafana)은 Spring EC2 8080을 스크랩하므로, codebomba로 쏴도 **서버측 지표는 똑같이 잡힌다**.

---

## 2. 사전 준비

### 2-1. Docker Desktop 실행
```powershell
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
docker info     # 에러 없이 나오면 OK (트레이 고래 아이콘 초록색 Running)
```

### 2-2. 배포 BE 살아있는지
```powershell
curl https://codebomba.com/actuator/health -UseBasicParsing    # {"status":"UP"}
```
> PowerShell `curl`(=`Invoke-WebRequest`)은 `-UseBasicParsing` 안 붙이면 "스크립트 실행 위험" 경고가 뜬다 — 서버 문제 아님, 무시 가능.

### 2-3. accessToken 확보 (가장 중요 — 아래 §3)

---

## 3. accessToken을 직접 주입해야 하는 이유 + 추출법

### 왜 `LOGIN_EMAIL/PASSWORD` 자동 로그인이 안 되나
배포는 **적응형 인증(step-up)** 이다. k6는 매번 **처음 보는 미신뢰 기기**라:
- 로그인 자체는 **200**으로 받아주지만,
- `accessToken` 대신 **`stepupToken`만 발급**하고 **이메일 OTP 추가 인증**을 요구한다.
- k6는 OTP를 못 푸니 → `[auth] 응답에 accessToken 쿠키가 없음` 에러로 죽는다.

→ **브라우저에서 정상 로그인(OTP 포함) 후 그 토큰을 복사해 주입**하는 게 유일한 방법.
(`accessToken`은 HttpOnly라 자동화·JS로는 못 읽는다. 자동 취득 불가.)

### 추출 방법
1. 브라우저에서 `codebomba.com` **정상 로그인** (OTP까지 완료)
2. F12 → **Application(애플리케이션)** → 좌측 **Cookies** → `https://codebomba.com` 클릭
3. 이름이 **`accessToken`** 인 행의 **Value** 전체 복사
   - `eyJ`로 시작, 점(`.`) 2개, **수백 자짜리 긴 문자열** (3글자 `eyJ...` 아님!)

> ⚠️ **토큰은 발급 후 1시간 만료**(payload `exp`). 복사 직후 바로 실행할 것. 만료되면 전부 401.
> ⚠️ 단명 토큰이라도 공개 채널에 올리지 말 것.

---

## 4. 실행

monitoring-deploy/ 폴더에서 (PowerShell):

```powershell
docker compose run --rm `
  -e BASE_URL="https://codebomba.com" `
  -e ACCESS_TOKEN="여기에_브라우저에서_복사한_긴_토큰_전체" `
  -e RESULT_NAME=chat-send-20vu `
  k6 run /scripts/chat/02-send-message-baseline.js
```

> 한 줄로 붙여도 됨(백틱 `` ` ``은 PowerShell 줄바꿈):
> ```powershell
> docker compose run --rm -e BASE_URL="https://codebomba.com" -e ACCESS_TOKEN="eyJ...전체..." -e RESULT_NAME=chat-send-20vu k6 run /scripts/chat/02-send-message-baseline.js
> ```

### 환경변수 정리
| 변수 | 역할 | 비고 |
|---|---|---|
| `BASE_URL` | 타깃 | codebomba 경로면 `https://codebomba.com`. 안 주면 기본 `43.200.241.157:8080`(직격) |
| `ACCESS_TOKEN` | 인증 토큰 직접 주입 | **필수**(step-up 우회). 없으면 login() 타다 실패 |
| `RESULT_NAME` | 결과 파일명 | `k6/results/<이름>-summary.md/json` |

> ❌ `-o experimental-prometheus-rw` **붙이지 마라** — 배포 키트엔 로컬 prometheus 없음(에러). 서버 지표는 배포 ③ Grafana에서 본다.

### VU(동시 사용자 수) 바꾸기
스크립트의 `options.stages` 를 수정한다 (현재 20 VU 고정):
```js
stages: [
    { duration: "10s", target: 20 },   // ← target 을 1, 5, 10 등으로
    { duration: "40s", target: 20 },
    { duration: "5s",  target: 0 },
],
```
> 진단 순서: **1 VU 단건 → 5 → 10 → 20** 으로 올리며 무릎점(knee) 찾기.

---

## 5. 결과 해석 — ⚠️ 숫자 착시 주의

SSE는 **스트림 시작과 동시에 무조건 200**을 준다. 그래서 AI가 실패해도 `http_req_failed`엔 안 잡히고, **200 본문 안 `event:error` 프레임**으로 들어간다.

| 지표 | 보는 법 |
|---|---|
| `http_req_failed` | ❌ **믿지 마라**. SSE라 거의 항상 0%로 나옴 |
| **`checks`** | ✅ **진짜 신호**. 100% 근처여야 정상. 낮으면 챗봇 응답이 실패 중 |
| `http_req_duration` | AI 생성 포함이라 **수 초**가 정상. **수십~백 ms로 빠르면 = 스트림이 빨리 죽는 중**(실패) |

### 체크 3종
1. `status is 200` — HTTP 도달 여부
2. `stream completed (event:done)` — **AI가 끝까지 응답했나** (정상 완료 지표)
3. `no stream error (event:error)` — AI 단 에러 없나

→ checks가 33% 근처면 **1번만 통과 = HTTP는 닿지만 AI 응답이 거의 다 실패**.

### 정상 vs 실패 예시
| | 정상 | 실패(동시성 한계) |
|---|---|---|
| checks | ~100% | ~34% |
| duration med | 수 초 | 137ms (빠르게 죽음) |
| 의미 | AI 끝까지 스트리밍 | event:error 다발 |

---

## 6. 서버측 관측 (배포 ③ Grafana / Loki)

http://13.124.63.188:3000 — **k6 터미널 = 클라 체감 / Grafana = 서버 내부**. 둘을 같이 봐야 병목이 네트워크냐 서버/DB냐 갈린다.

- **대시보드**: CPU·힙·HikariCP active·http p95
- **Loki 로그** (AI 실패 원인 추적 — 핵심):
  ```
  {job="spring"} |= "chat_ai_call_failed"     # FastAPI 호출 실패 (스택 포함)
  {job="spring"} |= "chat_stream_aborted"     # 종단 예외 + exceptionType
  {job="spring"} |= "chat_ai_error_chunk"     # AI 에러 청크 수신
  {job="spring"} |= "chat_stream_end"         # 정상/실패 1줄 기준선 (outcome, durationMs)
  ```
  → `exceptionType`/스택으로 **429(레이트리밋) / timeout / connection refused** 구분.

---

## 7. 트러블슈팅 (실제 겪은 순서)

| 증상 | 원인 | 해결 |
|---|---|---|
| `docker compose` 가 데몬에 못 붙음 | Docker Desktop 안 켜짐 | Docker Desktop 실행 후 `docker info` 확인 |
| `[auth] 응답에 accessToken 쿠키가 없음` | **step-up**(적응형 인증) — k6는 미신뢰 기기 | `-e ACCESS_TOKEN=`로 브라우저 토큰 주입 (§3) |
| `http_req_failed 100%`, checks 33%, **avg 17ms** | 토큰이 가짜(placeholder `eyJ...` 그대로 넣음) → 전부 401 | 진짜 토큰 전체를 붙여넣기 |
| `http_req_failed 0%`, checks ~34%, med 137ms | HTTP는 닿지만 **AI 단 실패 다발** (동시성 한계 유력) | Loki로 `exceptionType` 확인 → 1 VU부터 램프 |
| `connection refused` | 배포 BE 꺼짐 | EC2 start / health 확인 |
| 토큰 넣었는데 점점 401 증가 | 토큰 1시간 만료 | 브라우저에서 새로 복사 |

---

## 8. 테스트 후 정리

실 RDS에 쌓인 채팅방은 본인 계정 것이므로 앱의 **채팅방 삭제 API**(`DELETE /api/v1/chat/{roomId}`)나 화면에서 정리. 대량이면 운영 DB 정리 쿼리 별도 협의.
