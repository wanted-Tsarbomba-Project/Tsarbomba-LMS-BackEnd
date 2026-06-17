# 모니터링 / 부하테스트 실행 프로세스 (팀 공용)

> 이 문서는 **팀원 각자가 자기 도메인에 그대로 적용하는 실행 프로세스**다.
> 메트릭·로그 네이밍 등 "지켜야 하는 규칙"은 [`CONVENTION.md`](CONVENTION.md)가 단일 소스다. 여기선 **"어떤 순서로 무엇을 하는가"**만 다룬다.
> 도구 사용법·k6 개념·PromQL/LogQL 레퍼런스도 `CONVENTION.md`에 있다.

---

## 0. 큰 그림

```text
[Phase 0] 공통 기반 (1명=모니터링 리드가 먼저 구축, 1회)
   docker-compose 스택 · DomainTag · 완료로그 · Grafana 공통템플릿 · k6 공통lib(JWT)
      │
[Phase 1] 파일럿 (리드가 자기 도메인으로 7단계 완주 → 본보기 1세트 박제)
      │
[Phase 2] 팀원 복제 (각자 본보기를 복제해 자기 도메인에 7단계 적용)
```

- **왜 파일럿 먼저인가**: 프로세스 문서는 "이론"이 아니라 "실제로 돌려본 본보기"가 있어야 팀원이 따라온다. 리드가 한 바퀴 돌며 공통 기반을 실제로 검증한 뒤 박제한다.
- **팀 전체 통합 대시보드는 v2**다. 지금은 각자 로컬에서 **도메인별 대시보드**까지만 만든다. (단 네이밍 컨벤션은 지금부터 강제 → 나중에 통합이 공짜로 됨)
- 스택은 풀스택(Prometheus + Loki + Promtail + Grafana). 따라서 **구조화 로깅이 필수**다.

---

## Phase 0 — 공통 기반 (리드 1회 구축)

팀원이 시작하기 전에 아래가 git에 올라와 있어야 한다. 도메인 중립 공통 자산이다.

| # | 항목 | 내용 | 현재 상태 |
|---|------|------|-----------|
| 0-1 | 모니터링 스택 | `monitoring/docker-compose.yml`: Prometheus + Loki + Promtail + Grafana **+ loadtest MySQL(3307) + k6 컨테이너**. prometheus scrape(`host.docker.internal:8080/actuator/prometheus`), grafana datasource provisioning, k6는 `-o experimental-prometheus-rw`로 메트릭까지 전송 | ✅ **완료**(이미 동작) |
| 0-2 | HTTP 도메인 태그 | `DomainObservationConvention`(Boot 3 Observation API) — 핸들러 패키지(`...codebombalms.<domain>...`)에서 `domain` 태그 추출해 모든 `http_server_requests` 메트릭에 자동 부착. admin 중첩은 `admin`으로 묶음 | ✅ **완료** |
| 0-3 | 요청 완료 로그 | `MdcLoggingFilter`에 `event=request_started` / `event=request_completed method= uri= status= durationMs=` 추가(MDC `traceId` 동반). ⚠️ SSE 등 async 요청은 초기 디스패치 기준이라 durationMs가 스트림 전체를 못 잰다(알려진 한계) | ✅ **완료** |
| 0-4 | traceId | MDC `traceId` (이미 `MdcLoggingFilter`에 8자리 UUID 존재) | ✅ 있음 |
| 0-5 | 구조화 로깅 | `logback-spring.xml` → LogstashEncoder JSON(`logs/lms-app.log`), promtail이 `level`·`log_type` JSON 파싱 | ✅ **완료** |
| 0-6 | Grafana 공통 템플릿 | `domain` 변수 1개로 갈아끼우는 도메인 대시보드 템플릿(JSON) + k6 패널. 행 구성은 CONVENTION §9 | ❌ 신규 |
| 0-7 | k6 공통 lib | `monitoring/k6/lib/`: `config.js`(BASE_URL 등) · `summary.js`(handleSummary md/json) · `auth.js`(쿠키 기반 로그인 헬퍼 `login()`/`authCookies()`) | ✅ **완료** — ⚠️ loadtest DB에 로그인 가능한 시드 계정 필요(5단계) |

> **JWT 헬퍼가 교안에 없던 핵심 항목.** 우리 API는 대부분 `@PreAuthorize` 인증 필수다. k6에서 로그인→토큰 발급→`Authorization` 헤더를 모든 요청에 싣는 공통 헬퍼가 없으면 인증 API 부하테스트 자체가 불가능하다. (현재 `monitoring/k6/scripts/auth/`에 인증 스크립트가 시작돼 있음 — 공통 `auth.js`로 추출 권장)
>
> **남은 Phase 0 작업 = 0-6(Grafana 공통 대시보드 템플릿) 하나뿐.** 0-1~0-5·0-7은 완료(스택·도메인태그·완료로그·traceId·JSON로깅·k6 lib+auth.js). 0-7은 `auth.js` 추출까지 끝났고, 인증 시드 계정만 5단계에서 챙기면 된다.

---

## 공통 7단계 (Phase 1·2 동일)

각 팀원이 자기 도메인에 적용하는 순서. **1~5는 모든 도메인 동일, 6단계만 도메인 유형별로 갈린다.**

### 1단계 — 병목 후보 식별

자기 도메인을 아래 4유형 중 하나(또는 복수)로 분류하고, 병목 가설표를 만든다.

| 도메인 유형 | 병목 성격 | 예시 |
|-------------|-----------|------|
| 조회/집계형 | 인덱스·쿼리·N+1 | 목록 조회, 통계, 랭킹 |
| 외부연동/스트리밍형 | 외부 응답시간·커넥션풀·async 스레드·타임아웃 | 챗봇 SSE, 코드러너, 메일 |
| 쓰기/트랜잭션형 | 락·격리수준·커넥션풀 경합 | 주문·제출·동시 갱신 |
| 단순 CRUD형 | 사실상 병목 없음 | 단건 조회/수정 |

> **단순 CRUD형이면 "병목 없음, baseline이 기준 충족"도 정당한 완료다.** 억지 최적화 금지.

**산출물**: `bottleneck-hypothesis.md` — 대상 API · 가설 · 관찰 지표 · 성공 기준 (교안 §1 형식).

```text
[프롬프트 ①: 병목 후보 식별]
너는 백엔드 성능 엔지니어다. 아래 정보로 내 도메인의 병목 후보를 짚고 가설표로 정리해줘.
- 도메인/주요 API: (Method, URI 목록)
- 핵심 엔티티/연관관계: (엔티티, @OneToMany 등)
- 핵심 조회/쓰기 로직: (서비스 코드 또는 JPQL 붙여넣기)
관점: N+1 / 집계(group by, order by) / 외부 연동 지연 / 트랜잭션·락 경합 / 인덱스 부재.
출력: | 대상 API | 병목 가설 | 근거(코드 위치) | 관찰 지표 | 성공 기준 | 표.
```

### 2단계 — 커스텀 메트릭/로그 심기

[`CONVENTION.md`](CONVENTION.md)의 네이밍 규칙을 **반드시** 지켜 메트릭/로그를 심는다.

- 커스텀 메트릭: 이름에 도메인 prefix (`chat_message_send_duration_seconds` 등)
- HTTP/시스템 메트릭: Phase 0의 `domain` 태그로 자동 구분(추가 작업 없음)
- 구조화 로그: `event=<domain>_<verb> ... durationMs=... traceId=...`

**산출물**: `metrics.md` — 내가 심은 커스텀 메트릭 목록 + 의미.

### 3단계 — 성공 기준 수립

API 유형별 p95/실패율 기준을 잡는다(CONVENTION §성공기준 표 참조). **반드시 트래픽 조건 + 측정 기간을 포함**한다.

```text
[프롬프트 ②: 성공 기준]
아래 API 유형과 트래픽 조건으로 부하테스트 성공 기준을 잡아줘.
- API와 유형: (예: GET /api/v1/chat/list, 집계/조회형)
- 트래픽: (동시 VU, ramp-up, 측정 기간)
- 제약: 로컬, MySQL, 단일 모놀리식
출력: p95/p99/실패율 threshold(k6 형식) + 중단 기준 + 비즈니스 실패와 5xx 구분 기준.
```

### 4단계 — k6 시나리오 작성

`monitoring/k6/lib/` 공통 헬퍼(+`auth.js` JWT)를 import해서 `monitoring/k6/scripts/<domain>/*.js` 작성. baseline 최소 1 + 시나리오 1. 실행은 도커 k6 컨테이너(`monitoring/README.md` 참조).

```text
[프롬프트 ③: k6 시나리오] — 교안 §3의 7요소를 모두 채운다
너는 성능 테스트 엔지니어다. 아래로 k6 시나리오를 작성해줘.
- 시스템 역할 / 서비스 맥락(운영 상황)
- API 계약: Method, URI, request body, expected status
- 부하 모델: VU, duration, ramp-up, 트래픽 비율
- 성공 기준: p95, error rate, threshold
- 관찰 도구: Prometheus 메트릭, Loki 로그 쿼리
- 제약: 로컬, MySQL, 단일 앱, **JWT 인증 필요(monitoring/k6/lib/auth.js 사용)**
요구: thresholds·checks·sleep 포함, type 태그로 API 구분.
```

### 5단계 — baseline 측정

시드 데이터를 채운 뒤(아래 주의) k6 baseline 실행, 결과 저장.

> **시드 데이터 주의**: N+1·집계 병목은 데이터가 많아야 드러난다. 방 5개로는 N+1이 안 보인다. loadtest는 도커 MySQL(3307)을 쓰고 `application-loadtest.yml`의 `ddl-auto: create`로 매번 빈 스키마로 시작하므로, **부하 전 도메인별 시드 SQL을 `db/seed/`에 추가**해 "병목이 드러날 양"을 넣는다.
>
> 🚨 **반드시 `loadtest` 프로파일로 앱 실행**(`--spring.profiles.active=loadtest`). local로 부하 주면 운영 DB(RDS)로 트래픽이 가서 과금/장애 위험. (`monitoring/README.md` 경고 참조)

**산출물**: `results/before-*.md` (k6 summary).

### 6단계 — 병목 분석 → 최적화 (★ 도메인 유형별 분기)

baseline + Prometheus 수치 + Loki 로그를 교차 해석해 원인을 좁히고, 유형별 플레이북으로 최적화한다.

| 유형 | 최적화 플레이북 |
|------|-----------------|
| 조회/집계형 | `EXPLAIN` → 인덱스(`@Table(indexes=...)`) / fetch join / batch / DTO projection / (대량 시) 집계 테이블·캐시 |
| 외부연동/스트리밍형 | **최적화가 아니라 한계 측정**: WebClient 커넥션풀·MVC async 스레드·타임아웃 동작 검증. 외부 지연은 못 고침 → "몇 명까지 안 터지나"가 산출물 |
| 쓰기/트랜잭션형 | 트랜잭션 범위 축소 · 락/격리수준 · Hikari 풀 사이즈 · 재시도/낙관락 |
| 단순 CRUD형 | 최적화 불필요 — "병목 없음" 결론 |

```text
[프롬프트 ④: 병목 해석] — 교안 §14 상황별 해석틀
아래 데이터로 병목 원인을 좁혀줘.
- k6 결과: (p95, http_req_waiting, 실패율)
- Prometheus: (URI별 평균 응답시간, 커스텀 timer, Hikari active, CPU)
- Loki: (느린 요청 로그, durationMs, traceId 흐름)
질문: latency 증가가 DB인가 외부인가 락인가? 비즈니스 실패와 5xx를 구분해줘.
```

### 7단계 — 마이그레이션(반영) → 전후 비교

최적화를 **코드/스키마에 실제 반영**(인덱스·쿼리·페치·캐시 무엇이든)하고 **같은 조건으로 재측정**해 전후를 비교한다.

> **"마이그레이션"은 넓은 의미**다. Flyway가 없고 `ddl-auto: create`라, 인덱스는 `@Table(indexes=...)` 엔티티 어노테이션으로 반영한다(수동 `CREATE INDEX`는 재부팅 시 날아감). 쿼리/페치/캐시 변경도 모두 "마이그레이션"에 포함.

**산출물**: `results/after-*.md` + 전후 비교표(교안 §5.3 핵심값: p95, http_req_waiting p95, 커스텀 query duration, 실패율, EXPLAIN rows/key 변화).

---

## 도메인별 제출물 (Definition of Done)

각 팀원은 아래를 `monitoring/docs/domains/<domain>/`에 제출하면 "완료"다.

```text
monitoring/docs/domains/<domain>/
 ├─ bottleneck-hypothesis.md   # 1단계: 병목 가설표
 ├─ metrics.md                 # 2단계: 심은 커스텀 메트릭 목록
 ├─ compare.md                 # 7단계: 전후 비교표 (results 핵심값 정리 + 결론)
 └─ dashboard.json             # Grafana 도메인 대시보드 export

monitoring/k6/scripts/<domain>/*.js   # 4단계: 부하 시나리오 (코드)
monitoring/k6/results/*-summary.md    # 5·7단계: k6 raw 결과(before/after) — handleSummary 자동 저장
```

> k6 raw 요약(`*-summary.md`)은 `monitoring/k6/results/`에 자동 저장된다. 도메인 문서 폴더의 `compare.md`는 그중 핵심값(p95·waiting·실패율·custom duration·EXPLAIN)만 전후로 정리한 발표용 표다.

> 단순 CRUD형은 `results/after-*.md` 대신 "병목 없음, baseline 충족" 결론으로 갈음 가능.

---

## 파일럿 본보기 (리드 = 챗봇 도메인)

리드가 챗봇 도메인으로 7단계를 완주해 본보기 1세트를 만든다. 챗봇은 **두 트랙을 동시에** 보여줘 유형별 분기를 실제로 증명한다.

| 트랙 | 대상 | 성격 | 보여주는 것 |
|------|------|------|-------------|
| 최적화 서사 | `GET /api/v1/chat/list` | 조회형 N+1 (`ChatRoomQueryService.toResult`가 방 1개당 `problemTitlePort` 2회 호출) | baseline → N+1 확인 → fetch/batch 최적화 → 전후 비교 |
| 한계 측정 | `POST /api/v1/chat/messages` | 외부연동/스트리밍(SSE→FastAPI) | 외부 느릴 때 커넥션풀·async 스레드·타임아웃 한계 |

---

## 참고

- 규칙·레퍼런스: [`CONVENTION.md`](CONVENTION.md)
- 원본 교안: `K6.md`(Stage 4 k6 실습 교안) — 쇼핑몰 예시이나 개념·PromQL/LogQL·해석틀의 원천
