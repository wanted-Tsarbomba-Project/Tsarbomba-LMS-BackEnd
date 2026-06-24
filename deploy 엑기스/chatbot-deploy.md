# 챗봇(④) 배포 방법 — ECR + EC2 + GitHub Actions

> ⚠️ **현재 방식 변경됨(2026-06-24):** 배포는 **GitHub OIDC(키리스) + S3 + SSM `send-command`**, 접속은 **SSM Session Manager**, **22 닫힘**, chatbot 8000은 `cb-spring` 소스만. 아래의 `appleboy` SSH·scp·`.pem`·`EC2_SSH_*`·"22 SSH" 내용은 **초기 셋업 기록(학습용 보존)** 으로 현재 운영과 다르다. 현재 기준: 챗봇 레포 `.github/workflows/deploy.yml`(키리스) + [`ssm-vs-ssh.md`](ssm-vs-ssh.md) + [`OVERVIEW.md`](OVERVIEW.md).

CodeBomb LMS의 Python 챗봇(FastAPI)을 AWS EC2에 배포하는 전체 절차.
**처음(AWS 계정만 있는 상태)부터 배포·검증까지** 재현 가능하게 정리한다.

- 챗봇 레포: `wanted-Tsarbomba-Project/Tsarbomba-ChatBot-module03-LMS` (배포 브랜치 `main`)
- DB는 공용 RDS 사용 → `rds-setup.md` 선행. 모니터링(③)은 `../../monitoring-deploy/README.md`.

---

## 0. 아키텍처 / 방식

- **방식**: 생짜 EC2 + GitHub Actions (Elastic Beanstalk 아님)
- **모니터링과 다른 점**: 챗봇은 우리가 만든 코드라 **이미지를 빌드 → ECR push → EC2가 pull**. (모니터링은 기성 이미지라 ECR 불필요)
- **④ EC2 구성**: `chatbot(8000)` + `promtail`(로그 사이드카 → ③ Loki)
- **로그만 보냄**: 챗봇은 `/metrics` 미구현이라 Prometheus 스크랩 대상 아님. 로그만 promtail로 ③ Loki에 push.
- **흐름**:
  ```
  main 브랜치에 push
    → GitHub Actions (챗봇 레포)
       ├ docker build → ECR push (codebomb-chatbot)   [codebomb-ci AccessKey]
       ├ scp: deploy/* → ④ EC2 /opt/chatbot
       └ ssh: ECR login(EC2 Role) → docker compose pull & up
  ④ EC2 = chatbot(8000) + promtail(→ ③ Loki:3100)
  ```

---

## 1. AWS 선행 세팅 (최초 1회)

### 1-1. ECR 레포
ECR → 프라이빗 레포 생성 → 이름 **`codebomb-chatbot`** (리전 ap-northeast-2).
- **태그 변경 가능성(Tag immutability): 비활성(Mutable)** — 워크플로가 `:latest`를 매번 덮어쓰므로 immutable이면 배포 실패.

### 1-2. IAM (push용 / pull용 분리)
| 주체 | 용도 | 권한 | 자격증명 |
|---|---|---|---|
| **CI 사용자 `codebomb-ci`** | Actions가 ECR **push** | `AmazonEC2ContainerRegistryPowerUser` | **Access Key** (외부라 키 필요) |
| **EC2 Role `codebomb-ec2-ecr`** | EC2가 ECR **pull** | `AmazonEC2ContainerRegistryReadOnly` | **Role**(EC2 부착, 키 불필요) |

- Access Key는 생성 시 Secret이 **1회만** 표시됨 → 즉시 메모.
- EC2 Role은 **신뢰 주체를 "EC2"**로 만들어야 인스턴스 프로파일이 생겨 EC2에 부착 가능.
- 이 키/Role은 프론트(①)·Spring(②) 배포 때도 재사용.

### 1-3. ④ EC2 인스턴스
| 항목 | 값 |
|---|---|
| 이름 | `codebomb-chatbot` |
| AMI | Ubuntu Server 26.04 LTS **x86_64** (이미지가 amd64) |
| 타입 | **t3.micro** (SUT 아님·경량 → 프리티어 충분) |
| 키페어 | `codebomb-ec2-key` (전체 EC2 공유) |
| **IAM 인스턴스 프로파일** | **`codebomb-ec2-ecr`** ← 꼭 부착(누락 시 ECR pull 실패. 나중 부착도 가능) |
| 보안그룹 | 신규 `cb-chatbot` (아래) |
| 퍼블릭 IP | **활성** (Actions ssh 배포 + 인터넷 아웃바운드용. 끄면 NAT GW 비용 발생) |

`cb-chatbot` SG 인바운드:
| 포트 | 소스 | 용도 |
|---|---|---|
| 22 (SSH) | **0.0.0.0/0** | GitHub Actions ssh가 외부 러너에서 접속(키 인증) |
| 8000 | 내 IP → (나중) `cb-spring` SG | 챗봇 호출자. 운영 시 백엔드만 |

> 퍼블릭 IP를 자주 끄고 켤 거면 **EIP** 권장(IP 고정). 아니면 재시작마다 `CHATBOT_EC2_HOST` Secret 갱신.

### 1-4. ③ 모니터링 SG에 구멍 (promtail → Loki)
③ `cb-monitoring` SG 인바운드에 **3100 추가, 소스 = `cb-chatbot` SG**.
- 없으면 promtail이 Loki로 로그 push 못 함(챗봇 앱 자체는 정상 기동).

---

## 2. EC2 셋업 (SSH, 최초 1회)

```bash
# 접속 (PowerShell). pem 권한 에러 시:
#   icacls "<pem>" /inheritance:r ; icacls "<pem>" /grant:r "$($env:USERNAME):(R)"
ssh -i "C:\path\codebomb-ec2-key.pem" ubuntu@<퍼블릭IP>

# docker + aws-cli (aws-cli는 ECR login에 필요)
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
sudo snap install aws-cli --classic
exit                          # 그룹 적용 위해 재접속

# (재접속 후) 확인
docker compose version
aws --version

# 배포 디렉토리 + .env
sudo mkdir -p /opt/chatbot && sudo chown ubuntu:ubuntu /opt/chatbot
nano /opt/chatbot/.env
```

`/opt/chatbot/.env` (실제 값. **레포 커밋 금지**. `deploy/.env.example` 참고):
```bash
GEMINI_API_KEY=<실제 키>
GEMINI_MODEL=gemini-2.5-flash
DEFAULT_MAX_LENGTH=1000
DB_HOST=<RDS 엔드포인트>
DB_PORT=3306
DB_NAME=codebomb
DB_USERNAME=admin
DB_PASSWORD=<RDS 마스터 암호>
```
저장: `Ctrl+O` → Enter → `Ctrl+X`. 확인: `grep -o '^[A-Z_]*=' /opt/chatbot/.env`

---

## 3. 레포 파일 (챗봇 레포)

```
deploy/
├ docker-compose.yml      chatbot(ECR이미지, env_file:.env) + promtail
├ promtail-config.yml     도커 로그 → ③ Loki(사설IP):3100 push
└ .env.example            샘플 (실제 .env는 EC2에만)
.github/workflows/deploy.yml   build→ECR push→scp→ssh pull&up
Dockerfile                python:3.12-slim, uvicorn :8000 (기존)
```

- `docker-compose.yml`의 이미지: `${ECR_REGISTRY}/codebomb-chatbot:latest` — `ECR_REGISTRY`는 워크플로 ssh 스텝에서 `export`로 주입.
- `promtail-config.yml`의 Loki 타깃은 **③의 사설 IP**(stop/start로는 안 변하지만 ③ 재생성 시 갱신).

---

## 4. GitHub Secrets (⚠️ 챗봇 레포에, BE 레포 아님)

Settings → Secrets and variables → Actions:

| Secret | 값 |
|---|---|
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | `codebomb-ci` 키 |
| `CHATBOT_EC2_HOST` | ④ 퍼블릭 IP(또는 EIP. 순수 IP, `http://`·포트 X) |
| `EC2_SSH_USER` | `ubuntu` |
| `EC2_SSH_KEY` | `.pem` 전체 내용 (BEGIN~END, 줄바꿈 유지) |

> `.pem` 복사(PowerShell): `Get-Content "<pem>" | Set-Clipboard`

---

## 5. 배포

- **자동**: 챗봇 `main`에 push (= develop→main 머지 시) → 워크플로 자동 실행.
  - ⚠️ 워크플로 파일이 **`main` 브랜치에 존재**해야 발동.
  - `push` 트리거는 "main이 갱신되면" 발동(소스 브랜치 무관). main 직접 push도 트리거.
- **수동(첫 테스트 권장)**: Actions 탭 → "Deploy ChatBot (④ EC2)" → Run workflow.

스텝: 체크아웃 → AWS 자격증명 → ECR 로그인 → 빌드&푸시 → scp → ssh pull&up. 전부 초록불이면 성공.

---

## 6. 검증

```bash
# EC2에서
docker ps                         # lms-chatbot, lms-chatbot-promtail 둘 다 Up
docker logs lms-chatbot --tail 30 # "Application startup complete"
curl localhost:8000/health        # {"status":"ok"}
```
```powershell
# 로컬에서 (8000이 내 IP에 열려있을 때)
Invoke-RestMethod "http://<퍼블릭IP>:8000/health"   # status: ok
```
- **로그 연동**: Grafana(`http://<③EIP>:3000`) → Explore → Loki → `{container="lms-chatbot"}` → 챗봇 로그 보이면 promtail→Loki 정상.

---

## 7. 운영

- 측정/시연 때만 켜고 평소 stop(과금 절감). 퍼블릭 IP 사용 시 재시작마다 IP 갱신(또는 EIP).
- 네트워크 바뀌면 SG 22(이미 0.0.0.0/0이라 무관)·8000(내 IP) 소스 갱신.
- ② Spring 뜨면 `cb-chatbot` SG의 8000 소스를 내 IP → `cb-spring` SG로 교체.
- 코드 안 바뀌면 재기동: EC2에서 `cd /opt/chatbot && export ECR_REGISTRY=<acct>.dkr.ecr.ap-northeast-2.amazonaws.com && docker compose up -d` (단, ECR 토큰 만료 시 먼저 `aws ecr get-login-password ... | docker login ...`).

---

## 8. 트러블슈팅

| 증상 | 원인 / 해결 |
|---|---|
| Actions가 PR에서 안 돔 | 정상. `push: main`만 트리거(PR/`pull_request` 아님). 머지 후 발동 |
| IAM 인스턴스 프로파일에 Role 안 보임 | 새로고침 / Role 신뢰주체가 EC2 아니면 인스턴스 프로파일 미생성 → EC2용으로 재생성(또는 생성 후 부착) |
| ssh 스텝 `no such host` | `CHATBOT_EC2_HOST` 값 오류 → 순수 IP로 수정 후 Re-run |
| ECR pull `denied` | EC2에 `codebomb-ec2-ecr` Role 미부착, 또는 aws-cli 미설치 |
| 컨테이너 `Restarting`/`Exited` | `.env` 누락/오타(특히 `GEMINI_API_KEY`) → `docker logs lms-chatbot` |
| Grafana에 로그 안 옴 | ③ SG 3100 소스(cb-chatbot) 미저장 / `docker logs lms-chatbot-promtail`에 Loki 연결 에러 |
| `UNPROTECTED PRIVATE KEY` | Windows pem 권한 과다 → `icacls /inheritance:r` + 본인 읽기만 |
