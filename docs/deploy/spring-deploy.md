# ② Spring(LMS 백엔드) EC2 배포 가이드

> ⚠️ **현재 방식 변경됨(2026-06-24):** 배포는 **GitHub OIDC(키리스) + S3 + SSM `send-command`** 로, 접속은 **SSM Session Manager** 로 전환됐고 **22 인바운드는 닫혔다**. 아래의 `appleboy` SSH·scp·`.pem`·`EC2_SSH_*`·"22 열기" 내용은 **초기 셋업 기록(학습용 보존)** 이며 현재 운영과 다르다. 현재 기준은 각 레포 `.github/workflows/deploy.yml`(키리스) + [`ssm-vs-ssh.md`](ssm-vs-ssh.md) + [`OVERVIEW.md`](OVERVIEW.md). 비밀값 주입(`.env`)·헬스체크·DB명(`codebomba`) 등 EC2 셋업 본문은 여전히 유효.

> 처음 보는 사람도 그대로 따라 ② Spring을 EC2에 배포할 수 있게 정리. 방식은 **생짜 EC2 + GitHub Actions SSH(B방식)**, 이미지는 **ECR** 경유.
> 선행: RDS(`codebomb-rds`, AZ `ap-northeast-2c`)·③ 모니터링(Loki/Prometheus/Redis)이 떠 있어야 한다. → [`rds-setup.md`](rds-setup.md), `monitoring-deploy/README.md`

② Spring은 **부하테스트 대상(SUT)** 이다. ③ Prometheus가 `/actuator/prometheus`를 스크랩하고, promtail 사이드카가 로그를 ③ Loki로 보낸다.

---

## 0. 전체 그림

```
GitHub(deploy 브랜치 push)
  └─ Actions: docker build(=gradle bootJar) → ECR push → scp(deploy/**) → ssh(pull & up + 헬스체크)
② EC2(ap-northeast-2c)
  ├─ lms-spring  :8080  ──→ RDS codebomb(3306) / ③ Redis(6379) / ④ FastAPI(8000)
  └─ lms-promtail       ──→ ③ Loki(3100)
③ Prometheus ──스크랩──→ ② :8080/actuator/prometheus
```

---

## 1. 코드 산출물 (이 레포에 이미 포함)

| 파일 | 역할 |
|------|------|
| `Dockerfile` | 멀티스테이지(temurin17-jdk로 `bootJar` → 17-jre 런타임) |
| `.dockerignore` | 빌드 컨텍스트 축소 + `application-local.yml`/`.env`/`secrets` 유입 차단 |
| `src/main/resources/application-deploy.yml` | **deploy 프로파일**: DB/Redis/FastAPI를 env로, JWT 만료값, prometheus 노출 |
| `deploy/docker-compose.yml` | `spring`(ECR 이미지) + `promtail` 사이드카 |
| `deploy/promtail-config.yml` | 도커 로그 → ③ Loki(`172.31.39.210:3100`) |
| `deploy/.env.example` | EC2 `/opt/spring/.env` 샘플(자리표시자) |
| `.github/workflows/deploy.yml` | `main` push(develop→main 머지) → 빌드·푸시·배포 |

> `application-deploy.yml`은 `.gitignore`의 `application-*.yml` 무시 규칙에 **예외 등록**돼 있다(비밀값 없이 `${...}`만). 비밀값은 절대 여기 적지 말 것.

---

## 1-1. 설정(yml) 로딩 구조 — 여러 yml이 어떻게 합쳐지나

**base 하나는 항상 + 활성 프로파일 파일 하나가 그 위에 override.** 전부 읽는 게 아니다.

```
부팅 시 로드 = application.yml(항상) + application-{활성프로파일}.yml(하나만)
```

| 파일 | 언제 | 용도 |
|------|------|------|
| `application.yml` | **항상** | 공통 base(`${...}`+기본값). 내부 `---` 2번째 문서는 `on-profile: local,loadtest`라 그 둘일 때만 prometheus 노출 |
| `application-local.yml` | profile=local | 로컬 개발(옛 RDS·평문 비번) |
| `application-loadtest.yml` | profile=loadtest | 로컬 부하테스트(로컬 3307·ddl update) |
| `application-deploy.yml` | profile=deploy | **EC2 배포**(RDS codebomb·env 주입·JWT 만료·prometheus 노출) |

- **프로파일 결정**: 환경변수 `SPRING_PROFILES_ACTIVE`가 base의 기본값(`local`)을 덮어씀 → 배포 `.env`에 `=deploy`.
- **자리표시자 주입**: yml의 `${DB_PASSWORD}` 등은 컨테이너 환경변수(=`.env`)에서 치환.
- ⚠️ **deploy엔 base+deploy만 얹힘. local yml은 배포에 안 쓰임.** local에만 있고 deploy에 없는 값(예: `jwt.access-expiration`)은 placeholder 미해결로 **부팅이 깨지므로 deploy yml에 반드시 포함**해야 한다.

```
docker compose up → env_file:.env → 컨테이너 환경변수
  → Spring: SPRING_PROFILES_ACTIVE=deploy 읽음
  → application.yml + application-deploy.yml 로드(override)
  → ${...} 자리표시자를 환경변수로 치환
```

---

## 2. AWS 콘솔 준비 (1회)

### 2-1. ECR 레포
- 이름 `codebomb-spring`, Tag mutable. (챗봇·프론트와 동일 계정 `143924590342`, region `ap-northeast-2`)

### 2-2. 보안 그룹 `cb-spring`
| 방향 | 포트 | 소스 | 용도 |
|------|------|------|------|
| 인바운드 | 22 | 0.0.0.0/0 | SSH(Actions 러너는 외부 IP) |
| 인바운드 | 8080 | 0.0.0.0/0 | 브라우저 API 호출 + ③ Prometheus 스크랩 |

> 8080을 `0.0.0.0/0`로 열면 브라우저(프론트 `NEXT_PUBLIC_API_URL`은 브라우저→BE 직접 호출)·③ 스크랩이 모두 커버된다. 챗봇 8000(BE만 호출)과 달리 Spring은 공인 노출이 불가피. 운영 진입 시 HTTPS/ALB로 좁히기(백로그).

### 2-3. 다른 SG에 ② 인바운드 추가 (② SG 생성 후)
| 대상 SG | 포트 | 소스 | 용도 |
|---------|------|------|------|
| `codebomb-rds` | 3306 | `cb-spring` | ② → RDS |
| `cb-monitoring` | 6379 | `cb-spring` | ② → ③ Redis(OTP) |
| `cb-monitoring` | 3100 | `cb-spring` | ② promtail → ③ Loki |
| `cb-chatbot` | 8000 | `cb-spring` | ② → ④ FastAPI |

### 2-4. EC2 인스턴스
- **t3.small 이상**(SUT라 ④의 t3.micro보다 여유), Ubuntu, **AZ `ap-northeast-2c`**(RDS와 동일), 키페어 `codebomb-ec2-key`.
- IAM Role `codebomb-ec2-ecr`(ECR pull) 부착.
- 퍼블릭 IP 자동할당 또는 EIP.

### 2-5. EC2 초기 셋업 (SSH 접속 후)
```bash
# docker + compose
sudo apt-get update && sudo apt-get install -y docker.io docker-compose-plugin awscli
sudo usermod -aG docker ubuntu && newgrp docker
# 배포 디렉토리
sudo mkdir -p /opt/spring/secrets && sudo chown -R ubuntu:ubuntu /opt/spring
```
- `/opt/spring/.env` 작성: `deploy/.env.example` 복사 → 실제 값 채움(RDS 비번, ③ Redis 비번, ④ 사설IP, JWT/Mail/OAuth/GCP).
- `/opt/spring/secrets/gcp-storage-key.json` 배치(GCP 스토리지 키).

---

## 3. GitHub Secrets (BE 레포)

| 키 | 값 |
|----|----|
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | `codebomb-ci` AccessKey(ECR push) |
| `SPRING_EC2_HOST` | ② EC2 퍼블릭 IP (순수 IP, `http://`·포트 없이) |
| `EC2_SSH_USER` | `ubuntu` |
| `EC2_SSH_KEY` | `codebomb-ec2-key` .pem 전체 내용 |

> ①④와 공유되는 `EC2_SSH_USER`/`EC2_SSH_KEY`는 이미 있을 수 있다. ②용 `SPRING_EC2_HOST`만 추가.

---

## 4. 배포 실행

1. ② EC2 사설IP를 `monitoring-deploy/prometheus/prometheus.yml`의 `<SPRING_PRIVATE_IP>`에 기입 → ③ 재배포(워크플로 `monitoring-deploy` 또는 수동 `docker compose up -d`).
2. BE 레포 `deploy` 브랜치에 push(또는 Actions에서 `Deploy Spring (② EC2)` 수동 실행).
3. Actions: 빌드 → ECR push → scp → ssh `compose up` → `/actuator/health` 200 대기.

### 첫 부팅 스키마
- `/opt/spring/.env`에 `DDL_AUTO=update`를 둔 상태로 첫 배포 → 테이블 자동 생성.
- 부팅·동작 확인 후 `.env`에서 **`DDL_AUTO` 줄 삭제**(=`validate`로 고정) → 재기동.

---

## 5. 확인

```bash
# EC2에서
docker compose -f /opt/spring/docker-compose.yml ps   # lms-spring, lms-promtail Up
curl -s localhost:8080/actuator/health                # {"status":"UP"}
curl -s localhost:8080/actuator/prometheus | head     # 메트릭 노출 확인
```
- ③ Grafana: Prometheus 타깃 `spring` UP, Loki에 `job=spring` 로그 수신.

---

## 6. 트러블슈팅

| 증상 | 원인/조치 |
|------|-----------|
| `aws`/`docker: command not found` (ssh 단계 exit 127) | EC2에 미설치. `sudo apt-get install -y docker.io docker-compose-v2 awscli` + `usermod -aG docker ubuntu`. **박스 헷갈림 주의**(③ 모니터링에 설치하고 ②인 줄) — 프롬프트 끝 IP로 확인 |
| `ECR_REGISTRY variable is not set` (수동 기동) | `.env`에 `ECR_REGISTRY=143924590342.dkr.ecr.ap-northeast-2.amazonaws.com` 추가(Actions는 자동 주입) |
| **`Unknown database 'codebomb...'`(SQL 1049)** | 네트워크·인증은 정상, **DB만 없음**. 실제 DB명은 **`codebomba`** — `DB_URL`의 `/codebomba` 확인. 없으면 `CREATE DATABASE codebomba;` |
| 부팅 크래시인데 **로그가 안 보임** | `logback-spring.xml`의 `<root>` appender가 deploy 프로파일에 없으면 stdout 0 → 침묵. **`<springProfile name="deploy">`에 CONSOLE 추가** 필수. 임시 진단: `-e LOGGING_CONFIG=/tmp/console.xml` 마운트해 콘솔 강제 |
| `jwt.access-expiration` placeholder 미해결 부팅실패 | 만료값이 local yml에만 있음 → **deploy yml에 포함**(JwtTokenProvider가 기본값 없이 읽음) |
| 부팅 시 DB 연결 실패 | `cb-spring`→`codebomb-rds` 3306 인바운드, `DB_URL`/계정 확인. AZ 동일(2c)인지 |
| `Table ... doesn't exist`(validate) | 첫 부팅 `DDL_AUTO=update`로 생성 후 validate 전환했는지 |
| Redis 연결 실패 | `cb-monitoring` 6379 인바운드(from `cb-spring`) + `REDIS_PASSWORD`(requirepass) |
| ③ Prometheus 타깃 DOWN / `up=0` | prometheus.yml에 `<SPRING_PRIVATE_IP>` placeholder 남았는지 → ③에서 실제 사설IP로 교체 후 **`docker compose restart prometheus`**(자동 리로드 안 함) |
| Loki에 로그 없음 | `cb-monitoring` 3100 인바운드(from `cb-spring`) + **logback deploy 프로파일 배포됐는지**(로깅 없으면 보낼 게 없음) |
| `newgrp docker` 후 `exit` 시 권한 소실 | 도커 그룹은 새 로그인부터 적용 → SSH 재접속하면 sudo 없이 됨 |

---

## 6-1. FE 연동 — CORS / 쿠키

배포된 ① 프론트(브라우저)가 ② BE를 **교차 출처**로 호출하므로 CORS 허용이 필요하다.

- **증상**: 로그인 시 `blocked by CORS policy: No 'Access-Control-Allow-Origin'`.
- **원인**: `SecurityConfig`의 `allowedOrigins`가 `localhost`만 허용.
- **해결**: origin을 env화(`@Value("${cors.allowed-origins:...}")`) → `application-deploy.yml`의 `cors.allowed-origins: ${CORS_ALLOWED_ORIGINS}` → EC2 `.env`에 `CORS_ALLOWED_ORIGINS=http://<FE_EIP>:3000,http://localhost:3001`. **코드 변경이라 재배포 필요**(env만 바꿔선 구이미지에 안 먹음).
- **다음 복병(쿠키)**: FE·BE가 다른 출처 + HTTP라, 인증이 쿠키 기반이면 `SameSite=None; Secure`(HTTPS 필요)에 걸린다. JWT를 Authorization 헤더로 주고받으면 무관. 근본 해결은 HTTPS/도메인.

## 6-2. EIP(고정 IP) 운영

stop/start로 IP가 바뀌면 FE 번들·CORS·구글 redirect가 깨지므로 EIP로 고정한다.

- **할당·연결**: EC2 → 탄력적 IP → 할당 → 작업 → 연결 → 인스턴스 선택.
- **비용**: 켜진 인스턴스에 연결 시 거의 공짜, **미연결/꺼진 인스턴스면 시간당 과금**. 안 쓰는 EIP는 릴리스.
- IP 바뀐 뒤 갱신할 곳은 **[`OVERVIEW.md`](OVERVIEW.md) §3 IP 참조 맵** 참조.

---

## 7. 운영 전 정리(백로그)

- RDS 비번 재발급 + RDS/Redis SG `0.0.0.0/0` → 특정 소스로 축소.
- `DDL_AUTO` 제거(validate 고정), 스키마는 마이그레이션 도구로 관리 검토.
- 22 인바운드 0.0.0.0/0 → Actions 러너 대역 또는 배스천으로 축소 검토.
