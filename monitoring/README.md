# 📊 LMS 모니터링 & 부하테스트 스택

`docker compose` 한 방으로 모니터링 도구 전체를 실행/종료합니다.

| 도구 | 역할 | 접속 |
|------|------|------|
| **Grafana** | 메트릭 + 로그 통합 대시보드 | http://localhost:3000 (admin / admin) |
| **Prometheus** | 앱 메트릭 수집 (5초 간격) | http://localhost:9090 |
| **Loki** | 로그 저장소 | (Grafana에서 조회) |
| **Promtail** | `logs/*.log` JSON 로그를 Loki로 전송 | - |
| **MySQL 8.0** | 부하테스트 전용 DB | localhost:**3307** (root / loadtest) |
| **k6** | 부하 발생기 (필요할 때만 수동 실행) | - |

```
k6 ──부하──▶ Spring Boot 앱 (호스트, :8080)
                 │                    │
   /actuator/prometheus          logs/*.log
                 │                    │
            Prometheus            Promtail ──▶ Loki
                 └────────┬───────────┘
                       Grafana
```

---

## 🚀 시작하기

### 사전 준비

- Docker Desktop 실행 중일 것
- (부하테스트 시) 앱이 사용하는 Redis(6379)는 기존 개발 환경대로 각자 실행 — 이 스택에는 포함되지 않음

### 스택 실행 / 종료

| 명령어 (monitoring/ 에서) | 동작 | 데이터 |
|--------|------|--------|
| `docker compose up -d` | 전체 시작 | - |
| `docker compose down` | 전체 종료 ✅ **평소엔 이거** | 메트릭/로그/대시보드 **유지** — 다시 up 하면 그대로 |
| `docker compose down -v` | 종료 + 볼륨 삭제 ⚠️ | Prometheus 메트릭, Loki 로그, Grafana 설정, 부하테스트 DB **전부 삭제** |

> ⚠️ `-v` 는 완전 초기화가 필요할 때만! (예: 발표 리허설 전 데이터 정리)
> 단, `k6/results/` 의 테스트 결과 파일은 호스트에 저장되므로 `-v` 해도 **남습니다**.

---

## 🔥 부하테스트 절차

### 1. 앱을 loadtest 프로파일로 실행 (중요!)

```bash
# macOS / Linux
./gradlew bootRun --args='--spring.profiles.active=loadtest'

# Windows PowerShell
.\gradlew.bat bootRun --args='--spring.profiles.active=loadtest'

# Windows CMD (큰따옴표 필수!)
gradlew.bat bootRun --args="--spring.profiles.active=loadtest"
```

> 🚨 **반드시 loadtest 프로파일로!** local 프로파일로 부하를 주면 **AWS RDS로 트래픽이 가서 과금/장애 위험**이 있습니다.
> loadtest 프로파일은 도커 MySQL(3307)을 사용하며, 스키마는 시작 시 자동 생성됩니다.

### 2. k6 실행

```bash
docker compose run --rm k6 run -o experimental-prometheus-rw /scripts/<스크립트명>.js
```

결과는 3곳에서 확인:

- 터미널: 즉시 요약
- `k6/results/<이름>-summary.md` / `.json`: 자동 저장 (해석 가이드 포함)
- Grafana: k6 메트릭 + 서버 메트릭 실시간

### 3. 개선 전/후 비교 (발표용 ⭐)

`RESULT_NAME` 으로 결과 파일명을 바꿔 저장하면 덮어쓰지 않고 비교 가능:

```bash
docker compose run --rm -e RESULT_NAME=login-before-index k6 run -o experimental-prometheus-rw /scripts/01-login.js
# (개선 작업 후)
docker compose run --rm -e RESULT_NAME=login-after-index k6 run -o experimental-prometheus-rw /scripts/01-login.js
```

---

## 📈 결과 해석 기준

| 지표 | 의미 |
|------|------|
| `http_req_duration` **p95** | 주요 합격 기준 — 악화되면 사용자 체감 지연 |
| `http_req_failed` | 4xx/5xx 비율 — latency보다 먼저 확인 |
| `http_req_waiting` | 서버/DB 처리 지연 가능성 |
| p99 | 병목 "의심 신호"로만 해석 (표본 적으면 흔들림) |

> k6 지표만으로 원인을 확정하지 말고 **Prometheus(메트릭 추세) + Loki(로그/traceId)** 로 좁힐 것.

---

## 🛠️ 트러블슈팅

| 증상 | 원인 / 해결 |
|------|------------|
| `no configuration file provided: not found` | `monitoring/` 폴더 밖에서 실행함 → `cd monitoring` 후 실행 (또는 루트에서 `docker compose -f monitoring/docker-compose.yml up -d`) |
| Grafana 포트 3000 충돌 (`address already in use`) | 로컬에 brew 등으로 설치한 grafana가 점유 중 → `brew services stop grafana prometheus` |
| Prometheus 타겟 `lms-app` DOWN | 앱이 안 떠 있거나 Actuator 미설정 — 앱 실행 + `/actuator/prometheus` 200 확인 |
| Grafana에 데이터소스 없음 | `grafana/provisioning/datasources/datasources.yml` 내용 확인 후 `docker compose restart grafana` |
| `datasources.yml` 에 IDE 경고 "Unknown API version" | IntelliJ가 k8s 파일로 오인한 것 — 무시 |

---

## 🚧 현재 상태 / 예정

- [x] 모니터링 스택 6종 docker-compose 구성
- [x] loadtest 프로파일 (도커 MySQL)
- [ ] 앱 Actuator + Micrometer 의존성 (메트릭 노출) — 예정
- [ ] k6 공통 설정(`lib/config.js`) + 시나리오 스크립트 — 예정
- [ ] Grafana 대시보드 (Spring Boot / JVM) import — 예정
