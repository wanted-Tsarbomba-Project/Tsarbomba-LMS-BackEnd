# 배포 BE 부하테스트 (monitoring-deploy)

> **배포된 ② Spring(EC2)** 에 k6로 부하를 주고, **배포 ③ Grafana로 서버를 관측**하는 키트.
> 로컬 앱 부하는 [`../monitoring-local`](../monitoring-local) 을 쓴다 — 여긴 **배포 전용**.

| | monitoring-local | **monitoring-deploy (여기)** |
|---|---|---|
| 대상 | 내 PC의 로컬 앱(loadtest 프로파일, 도커 MySQL) | **배포 ② Spring `43.200.241.157:8080`** (실 RDS) |
| 관측 스택 | 로컬 prometheus/grafana/loki 직접 띄움 | **안 띄움** — 배포 ③ Grafana가 이미 BE 스크랩 |
| 구성 | 풀스택 compose | **k6 단독** |

---

## ⚠️ 안전 — 이것부터 읽어라

배포 BE는 **진짜 RDS(codebomba)** 를 쓴다. 그래서:

- ✅ **읽기(GET) 시나리오만** 여기 담겨 있다. 쓰기/생성류(`badge-sync`·`hide-today`·`generation`·`operation-rule-run`)는 **일부러 뺐다** → 그건 `monitoring-local`(격리 DB)에서만.
- ✅ **계정은 전용 테스트 계정**을 써라. 일반 사용자 계정으로 로그인 부하 주지 말 것.
- ❌ VU를 무리하게 올리지 마라. BE는 t3.small(SUT) — README의 기본 스테이지 수준으로.

---

## 시나리오 티어

### Tier 0 — 무인증·무시드 (지금 바로, 가장 안전) ⭐ 여기서 시작
| 스크립트 | 엔드포인트 | 비고 |
|---|---|---|
| `auth/01-email-check.js` | `GET /auth/check/email` | 랜덤 이메일 중복확인. 로그인·시드 불필요, 순수 읽기 |

### Tier 1 — 실계정 로그인 + 시드 데이터 필요
> `setup()`에서 **1회 로그인**(POST /auth/login) 후 GET. **배포 RDS에 해당 계정·데이터가 있어야** 동작/의미가 있다. 로그인 계정은 `-e LOGIN_EMAIL=... -e LOGIN_PASSWORD=...` 로 주입.

| 스크립트 | 엔드포인트 | 필요 권한 |
|---|---|---|
| `user/01-student-list-baseline.js` | `GET /users` | **ADMIN** 계정 + 학생 다수 시드 |
| `admin/01-alert-list-baseline.js` | `GET /admin/alerts...` | ADMIN |
| `admin/02-alert-detail-baseline.js` | `GET /admin/alerts/{id}...` | ADMIN |
| `ranking/01-ranking-baseline.js` | `GET /ranking...` | 일반 사용자 |
| `learning/01-student-progress-baseline.js` | `GET /learning...` | 일반 사용자 |
| `recommendation/01-list-baseline.js` | `GET /recommendation...` | 일반 사용자 |
| `chat/01-list-baseline.js` | `GET /chat/list` | 일반 사용자 |

### 제외 (쓰기/생성 — 로컬 전용, 여기 없음)
`admin/03-operation-rule-run`, `badge/01-badge-sync`, `recommendation/02-hide-today`, `recommendation/03-generation`

---

## 실행

### 0. 사전
- Docker Desktop 실행 중
- 배포 BE 살아있는지: `curl http://43.200.241.157:8080/actuator/health` → `{"status":"UP"}`
- 배포 ③ Grafana 열어두기: `http://13.124.63.188:3000` (Spring/JVM 대시보드)

### 1. Tier 0 — 바로 쏘기 (`monitoring-deploy/` 에서)
```bash
docker compose run --rm k6 run /scripts/auth/01-email-check.js
```
> `BASE_URL` 기본값이 배포 BE라 **따로 안 줘도 배포로 간다**(`k6/lib/config.js`).
> ⚠️ `-o experimental-prometheus-rw` **붙이지 마라** — 그건 로컬 prometheus로 push하는 옵션. 배포 ③ Prometheus가 이미 BE를 스크랩하므로 불필요(붙이면 에러).

### 2. Tier 1 — 실계정으로 쏘기
```bash
docker compose run --rm \
  -e LOGIN_EMAIL=admin@test.com -e LOGIN_PASSWORD='****' \
  k6 run /scripts/user/01-student-list-baseline.js
```

### 전/후 비교용 이름
```bash
docker compose run --rm -e RESULT_NAME=user-list-deploy k6 run /scripts/user/01-student-list-baseline.js
```

---

## 결과 보기

| 어디 | 무엇 |
|---|---|
| 터미널(끝나면) | `http_req_duration p95`, `http_req_failed` — 합격선 통과? |
| `k6/results/<이름>-summary.md` / `.json` | 자동 저장(해석 가이드 포함). 콘솔에도 `monitoring-deploy/k6/results/` 기준으로 표기된다. |
| 배포 ③ Grafana | **서버측** CPU·힙·HikariCP active·http p95 — 병목 위치 |
| 배포 ③ Grafana → Loki | `{job="spring"}` — 느린 요청/에러 로그 |

> k6 터미널 = **클라이언트 체감**(네트워크 포함), Grafana = **서버 내부**. 둘을 같이 봐야 병목이 네트워크냐 서버/DB냐 갈린다.

---

## 트러블슈팅

| 증상 | 원인 / 해결 |
|---|---|
| `[auth] 로그인 실패 status=401` | 배포 RDS에 그 계정 없음 → `-e LOGIN_EMAIL/PASSWORD`를 **배포에 실제 있는 계정**으로 |
| Tier1 GET이 403 | 권한 부족 → ADMIN 필요한 스크립트는 admin 계정으로 |
| 응답은 200인데 데이터 빈약 | 배포 RDS에 시드가 적음 → 부하 의미 약함(로컬 시드 환경과 다름) |
| `connection refused` | 배포 BE 꺼져있음 → EC2 start / health 확인 |
| Grafana에 k6 지표 안 보임 | 정상 — 여긴 서버측만 본다(k6 클라 지표는 터미널/results). |
