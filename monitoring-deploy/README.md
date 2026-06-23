# 모니터링 배포 방법 (③ EC2)

CodeBomb LMS 모니터링 스택(Prometheus·Loki·Grafana·Redis)을 AWS EC2에 배포하는 전체 절차.
**처음(AWS 계정만 있는 상태)부터 배포·검증까지** 재현 가능하게 정리한다.

> 로컬 부하테스트용 스택은 `../monitoring-local/`. 이 디렉토리(`monitoring-deploy/`)는 **EC2 배포 전용**이다.

---

## 0. 아키텍처 / 방식

- **배포 방식**: 생짜 EC2 + GitHub Actions SSH (Elastic Beanstalk 아님)
- **모니터링은 ECR 불필요** — prom/grafana/loki/redis 전부 기성 이미지라 Docker Hub에서 직접 pull. compose 파일과 설정만 EC2로 scp 후 `docker compose up`.
- **③ EC2 구성**: `prometheus(9090) + loki(3100) + grafana(3000) + redis(6379)`
- **흐름**:
  ```
  deploy 브랜치에 monitoring-deploy/** push
    → GitHub Actions
       ├ scp: monitoring-deploy/* → ③ EC2 /opt/monitoring
       └ ssh: docker compose --env-file .env up -d
  ```

---

## 1. AWS 기본 세팅 (최초 1회)

### 1-1. IAM (3종)
| IAM | 용도 | 권한 |
|---|---|---|
| 작업용 사용자 (`codebomb-admin`) | 콘솔 관리. root 대신 | `AdministratorAccess` |
| CI 사용자 (`codebomb-ci`) | GitHub Actions ECR push (챗봇/프론트/Spring용) | `AmazonEC2ContainerRegistryPowerUser` + Access Key |
| EC2 Role (`codebomb-ec2-ecr`) | EC2가 ECR pull (①②④용) | `AmazonEC2ContainerRegistryReadOnly` |

> 모니터링(③)은 ECR 안 쓰므로 CI 사용자·EC2 Role 불필요. 위 ②③은 챗봇/프론트/Spring 단계에서 쓴다.

### 1-2. 키페어
EC2 → 네트워크 및 보안 → 키 페어 → 생성. 이름 `codebomb-ec2-key`, **RSA / .pem**. (리전 ap-northeast-2). 전체 EC2가 이 키 하나 공유.

### 1-3. 보안그룹 (모니터링용, 이름 `sg-` 불가 → 예 `codebomb-mon`)
| 포트 | 소스 | 용도 |
|---|---|---|
| 22 (SSH) | 내 IP | 접속·배포 |
| 9090 (Prometheus) | 내 IP | UI (인증 없음 — 절대 공개 금지) |
| 3000 (Grafana) | `0.0.0.0/0` | 팀 공유 (비번·가입/익명차단으로 방어) |
| 6379 (Redis) | (나중) sg-spring | OTP. ② 생성 시 추가 |
| 3100 (Loki) | (나중) sg-spring·sg-chatbot | ②④ promtail push. ②④ 생성 시 추가 |

- 아웃바운드: 기본(전체 허용) 유지.
- "내 IP"는 현재 공인 IP. **네트워크 바뀌면(집·카페·핫스팟) 22·9090 소스 갱신** 필요.

### 1-4. EC2 인스턴스
| 항목 | 값 |
|---|---|
| AMI | Ubuntu Server 26.04 LTS **x86_64** (앱 이미지가 amd64라 Arm 금지) |
| 타입 | **t3.small** (RAM 2GB — 컨테이너 4개. micro는 OOM. ⚠️프리티어 아님) |
| 키페어 | codebomb-ec2-key |
| 보안그룹 | 기존 선택 → codebomb-mon |
| 퍼블릭 IP | 활성 |
| 스토리지 | gp3 20GB |

### 1-5. EIP (선택, 권장)
네트워크 → 탄력적 IP → 할당 → 인스턴스에 연결. IP 고정(stop/start해도 안 바뀜).
- ⚠️ 미연결 방치 시 과금. terminate 후엔 **릴리스**해야 과금 정지.

---

## 2. EC2 셋업 (SSH, 최초 1회)

```bash
# 접속 (PowerShell). pem 권한 에러 시: icacls <pem> /inheritance:r ; icacls <pem> /grant:r "%USERNAME%:(R)"
ssh -i "C:\path\codebomb-ec2-key.pem" ubuntu@<EIP>

# docker 설치
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
exit                       # 그룹 적용 위해 재접속
# (재접속 후) 확인
docker compose version
docker ps

# 배포 디렉토리 + .env
sudo mkdir -p /opt/monitoring
sudo chown ubuntu:ubuntu /opt/monitoring
nano /opt/monitoring/.env
```

`.env` 내용 (비번은 `openssl rand -base64 18`로 강하게, git 커밋 금지):
```bash
GRAFANA_USER=admin
GRAFANA_PASSWORD=<랜덤 16자+>
REDIS_PASSWORD=<랜덤 16자+>
```

---

## 3. GitHub Secrets (BE 레포)

Settings → Secrets and variables → Actions:

| Secret | 값 |
|---|---|
| `MON_EC2_HOST` | EIP (순수 IP만. `http://`·포트 X) |
| `EC2_SSH_USER` | `ubuntu` |
| `EC2_SSH_KEY` | `.pem` 내용 전체 (BEGIN~END 포함, 줄바꿈 유지) |

---

## 4. 파일 구조

```
monitoring-deploy/
├ docker-compose.yml          prom+loki+grafana+redis
├ prometheus/prometheus.yml   ② scrape + remote-write
├ grafana/provisioning/       datasources(prometheus/loki) + dashboards
├ .env.example                샘플 (실제 .env는 EC2에만)
└ .gitignore                  .env
.github/workflows/monitoring-deploy.yml   배포 워크플로
```

---

## 5. 배포

- **자동**: `monitoring-deploy/**`를 `deploy` 브랜치에 push → 워크플로 자동 실행
- **수동**: Actions 탭 → "Deploy Monitoring (③ EC2)" → Run workflow (workflow_dispatch)
- secret 고친 뒤 재시도: 실패한 run → **Re-run jobs**

---

## 6. 검증

```bash
# EC2에서
docker ps        # prometheus/loki/grafana/redis 4개 Up
```
- 브라우저 `http://<EIP>:3000` → Grafana 로그인(admin / .env 비번)
- Grafana → Connections → Data sources에 Prometheus·Loki 자동 등록 확인
- 팀원은 Grafana에서 **Viewer 계정** 발급해 공유

> Prometheus의 spring 타깃은 `<SPRING_PRIVATE_IP>` 플레이스홀더라 "down"이 정상. ② Spring 생성 후 채운다.

---

## 7. 운영

- **측정/시연 때만 켜고 평소 stop** (t3.small 과금 절감). EIP라 start해도 IP 유지.
- 코드 안 바뀌면 start 후 `cd /opt/monitoring && docker compose --env-file .env up -d`로 재기동.
- **네트워크 바뀌면** SG 22·9090 소스를 현재 IP로 갱신.
- 프로젝트 종료 시: EC2 terminate + **EIP 릴리스** + 키페어 정리.

---

## 8. 트러블슈팅

| 증상 | 원인 / 해결 |
|---|---|
| SSH `Connection timed out` | SG 22 소스가 현재 IP 아님 → "내 IP" 갱신 (네트워크 바뀜) |
| `UNPROTECTED PRIVATE KEY` | Windows pem 권한 과다 → `icacls /inheritance:r` + 본인 읽기만 |
| scp `no such host` | `MON_EC2_HOST` 값 오류 → 순수 IP로 수정 후 Re-run |
| `docker` permission denied | usermod 후 재접속 안 함 → exit 후 재접속 |
| Grafana 접속 안 됨 | SG 3000 / 컨테이너 Up / EIP 확인 |
