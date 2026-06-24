# 배포 전체 개요 (CodeBomb LMS)

> 5개 박스(① 프론트 · ② 스프링 · ③ 모니터링 · ④ 챗봇 + RDS)의 배포 현황·IP·참조 관계를 한곳에 모은 **단일 지도**.
> 방식(2026-06-24~): **GitHub OIDC(키리스) + S3 아티팩트 + SSM `send-command`**, 사람 접속은 **SSM Session Manager**(22 전부 닫힘, `.pem` 폐기). 이미지는 **ECR**(모니터링만 기성 이미지). 계정 `143924590342`, 리전 `ap-northeast-2`, AZ `ap-northeast-2c`. (옛 SSH/`.pem` 방식 기록은 [`ssm-vs-ssh.md`](ssm-vs-ssh.md)·각 deploy.md 배너 참조)
> 서비스별 상세 절차: [`spring-deploy.md`](spring-deploy.md) · [`chatbot-deploy.md`](chatbot-deploy.md) · [`rds-setup.md`](rds-setup.md) · ③ 모니터링은 **별도 레포** `wanted-Tsarbomba-Project/Tsarbomba-Monitoring`(README 참조)

---

## 1. 구성도

```
[사용자 브라우저]
   ├─ http://13.125.139.116:3000 ──────────→ ① 프론트(Next.js, ECR)
   └─ http://43.200.241.157:8080 ──────────→ ② 스프링(SUT, ECR)   ← NEXT_PUBLIC_API_URL로 브라우저가 직접 호출
② 스프링 ──사설망──→ RDS(3306) / ③ Redis(6379) / ④ FastAPI(8000)
②④ promtail ──사설망──→ ③ Loki(3100)
③ Prometheus ──사설망 스크랩──→ ②:8080/actuator/prometheus
```

---

## 2. 박스별 현황

| 박스 | EC2 | 퍼블릭(EIP) | 사설 IP | 포트 | ECR | SG | 프로파일/비고 |
|------|-----|------------|---------|------|-----|----|----|
| ① 프론트 | `codebomb-frontend` | `13.125.139.116` | - | 3000 | `codebomb-frontend` | `cb-frontend` | NEXT_PUBLIC_API_URL 빌드주입 |
| ② 스프링 | `codebomb-spring` | `43.200.241.157` | `172.31.45.217` | 8080 | `codebomb-spring` | `cb-spring` | profile `deploy`, AZ 2c, SUT |
| ③ 모니터링 | `codebomb-monitoring` | `13.124.63.188` | `172.31.39.210` | 3000/9090/3100/6379 | (기성) | `cb-monitoring` | Grafana/Prom/Loki/Redis |
| ④ 챗봇 | `codebomb-chatbot` | `43.200.59.195` | `172.31.47.238` | 8000 | `codebomb-chatbot` | `cb-chatbot` | FastAPI, t3.micro |
| RDS | `codebomb-rds` | `codebomb-rds...rds.amazonaws.com`(엔드포인트, ↓표 아래 전체) | - | 3306 | - | `codebomb-rds` | MySQL8, DB **`codebomba`**, AZ 2c |

- RDS 엔드포인트: `codebomb-rds.cb48qiuki6pn.ap-northeast-2.rds.amazonaws.com`
- 접속: **SSM Session Manager**(`aws ssm start-session --target i-...` 또는 콘솔 Connect). 공유 키페어 `codebomb-ec2-key.pem`은 **폐기**(22 닫힘으로 무력). 박스 구분은 프롬프트 끝 사설IP로(`45-217`=②, `39-210`=③).

---

## 3. ⭐ IP 참조 맵 — IP 바뀌면 여기 다 고쳐야 한다

> EIP라 평소엔 안 바뀌지만, 박스 교체/EIP 재할당 시 **퍼블릭 IP를 참조하는 모든 곳**을 갱신해야 한다. (사설 IP는 stop/start에도 안 바뀜)

### ② 스프링 퍼블릭 IP(`<BE_EIP>`)를 참조하는 곳
| 위치 | 키 | 값 |
|------|----|----|
| FE GitHub **Variable** | `NEXT_PUBLIC_API_URL` | `http://<BE_EIP>:8080` (바꾸면 **FE 재빌드** 필수) |
| FE GitHub **Secret** | — | — |
| BE GitHub **Secret** | `SPRING_EC2_HOST` | `<BE_EIP>` (배포 SSH 대상) |
| BE EC2 `/opt/spring/.env` | `GOOGLE_REDIRECT_URI` | `http://<BE_EIP>:8080/api/v1/auth/oauth2/callback/google` |
| 구글 클라우드 콘솔 | 승인된 redirect URI | 위 주소 등록(미등록 시 `redirect_uri_mismatch`) |

### ① 프론트 퍼블릭 IP(`<FE_EIP>`)를 참조하는 곳
| 위치 | 키 | 값 |
|------|----|----|
| BE EC2 `/opt/spring/.env` | `CORS_ALLOWED_ORIGINS` | `http://<FE_EIP>:3000,http://localhost:3001` (바꾸면 **BE 재배포**) |
| FE GitHub **Secret** | `FRONTEND_EC2_HOST` | `<FE_EIP>` (배포 SSH 대상) |

### 사설 IP를 참조하는 곳 (stop/start에도 안 바뀜 — 박스 재생성 시만)
| 참조자 | 대상 | 키/위치 |
|--------|------|---------|
| ③ Monitoring 레포 `prometheus/prometheus.yml` | ② 사설 `172.31.45.217:8080` | scrape target |
| ② `/opt/spring/.env` | ③ 사설 `172.31.39.210` | `REDIS_HOST` |
| ②④ `deploy/promtail-config.yml` | ③ 사설 `172.31.39.210:3100` | Loki push url |
| ② `/opt/spring/.env` | ④ 사설 `172.31.47.238:8000` | `FASTAPI_URL` |

> **현재 값 충돌 주의**: CORS origin = **FE IP**, GOOGLE_REDIRECT = **BE IP**. 서로 다른 박스다.

---

## 4. 보안그룹(SG) 매트릭스 — "받는 쪽"에만 인바운드를 연다

> 상태(2026-06-24): **22 전부 닫힘**(접속=SSM). 사람·CI 접속 인바운드 0.

| SG(받는 쪽) | 포트 | 소스 | 용도 |
|------|------|------|------|
| `cb-frontend` | 3000 | 0.0.0.0/0 | 브라우저 (22 닫힘→SSM) |
| `cb-spring` | 8080 | 0.0.0.0/0 | 브라우저·스크랩 (22 닫힘→SSM) |
| `cb-monitoring` | 3000 | 0.0.0.0/0 | Grafana(자체 로그인) (22 닫힘→SSM) |
| `cb-monitoring` | ~~9090~~ | **닫힘** | Prometheus UI → **SSM 포트포워딩**으로 접근 |
| `cb-monitoring` | 3100 | `cb-chatbot`, `cb-spring` | promtail→Loki |
| `cb-monitoring` | 6379 | `cb-spring` | Redis(OTP) |
| `codebomb-rds` | 3306 | `cb-spring` (+ 임시 0.0.0.0/0 — **4-C에서 제거 예정**, SSM RemoteHost 터널로 대체) | DB |
| `cb-chatbot` | 8000 | `cb-spring` | BE→FastAPI (0.0.0.0/0 제거됨, 22 닫힘→SSM) |

> 원칙: "내가 나가는 것"은 아웃바운드(기본 전체허용)라 규칙 불필요. **받는 쪽 SG에만** 인바운드. 메일(587)·ECR(443) 등 외부행은 손 안 댐. 22·9090은 SSM이 대체(인바운드 0).

---

## 5. 배포 트리거

| 워크플로 | 레포 | 트리거 | 비고 |
|----------|------|--------|------|
| ② Spring (`deploy.yml`) | BE | `push: [main]` + `workflow_dispatch` | 안정화 전엔 dispatch로 `deploy` 브랜치 수동 배포 |
| ③ Monitoring (`deploy.yml`) | **Monitoring**(별도 레포) | `push: [main]` + `workflow_dispatch` | OIDC 키리스(S3+SSM), `codebomb-github-actions` 역할 재사용 |
| ① Frontend (`deploy.yml`) | FE | `push: [main]` | NEXT_PUBLIC 빌드주입 |
| ④ Chatbot (`deploy.yml`) | Chatbot | `push: [main]` | |

- **수동 배포(머지 없이)**: Actions → 해당 워크플로 → **Run workflow → branch 선택 → Run** (`workflow_dispatch`).
- **정식 릴리스**: deploy→develop→main 머지 → main 트리거.

---

## 6. 운영 메모

- **비용 절약**: 부하테스트/데모 안 할 땐 EC2 **stop**. EIP라 IP 유지돼서 다시 켜도 안 깨짐(끈 동안 EIP 소액 과금).
- **시크릿/변수 분리**: 배포용→GitHub Secrets, 앱 비밀값→EC2 `/opt/<svc>/.env`, 비밀 아닌 빌드설정→GitHub Variables.
- **백로그(운영 전)**: RDS 비번 재발급·SG `0.0.0.0/0` 축소, HTTPS/도메인(ALB)로 CORS·쿠키 정상화, `DDL_AUTO` 제거(validate 고정), 22 인바운드 축소.
