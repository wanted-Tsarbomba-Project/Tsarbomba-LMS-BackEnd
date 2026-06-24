# 내 도메인 모니터링/부하테스트 — 이것만 보고 진행하세요

> 처음이어도 **위에서부터 순서대로** 따라 하면 끝납니다.
> "왜 하는지"를 같이 적어둡니다. 이유를 알아야 자기 도메인에 맞게 응용할 수 있어요.
>
> 🧭 **가장 먼저** → [`../purpose.md`](../purpose.md): 이 모니터링의 두 용도(성능 개선 / 오류 모니터링)와 **지금 우리가 하는 건 ① 성능 개선**이라는 범위. (혼란 방지용, 1분)
>
> - 메트릭/Loki를 **스스로 설계**하는 법 → [`metrics-and-loki-guide.md`](metrics-and-loki-guide.md)
> - 조회(read) 도메인 **완성 예시** → [`example-read-list.md`](example-read-list.md)
> - 도구 실행법(스택 켜기/끄기) → [`../../README.md`](../../README.md)

---

## 0. 우리가 왜 이걸 하나

부하테스트는 "요청 많이 보내기"가 아니라 **"내 도메인이 트래픽을 견디는지, 못 견디면 어디가 병목인지"를 숫자로 증명**하는 일입니다.

증명에는 세 도구가 한 팀입니다:

| 도구 | 뭘 답하나 | 비유 |
|---|---|---|
| **k6** | 부하를 만든다 + 사용자 관점 응답시간(p95) | 운동시키는 코치 |
| **Prometheus/Grafana** | 서버 내부 수치 추세 (어디가 느린가) | 심전도·혈압 모니터 |
| **Loki** | 그 순간 무슨 일이 있었나 (왜 느린가) | 진료 기록 |

> 메트릭 = "얼마나/추세", 로그 = "왜". 둘은 경쟁이 아니라 **릴레이**입니다. 메트릭으로 이상을 잡고 → 로그로 원인을 좁힙니다.

---

## 사전 준비 (한 번만)

1. **Docker Desktop** 실행 (고래 아이콘)
2. `git pull` (develop 최신화)
3. 모니터링 스택 켜기:
   ```bash
   cd monitoring-local
   docker compose up -d
   docker compose ps    # prometheus/grafana/loki/promtail/mysql 5개 Up 이면 OK
   ```

### ⚠️ 처음 겪는 함정 (미리 알아두면 시간 아낌)

| 증상 | 원인 / 해결 |
|---|---|
| 앱이 RDS(운영 DB)에 붙음 🚨 | IntelliJ **Active profiles = `local,loadtest`** 로 실행. 순서 중요(뒤의 loadtest가 이김 → 도커 MySQL 3307). 끝나면 **비워서** 평소 개발로 복귀 |
| `Unable to determine Dialect` 부팅 실패 | 도커 MySQL이 안 떴거나 3307 포트 미노출. `docker compose up -d mysql` 후 `docker compose ps` 에서 `0.0.0.0:3307->3306` 확인 |
| Grafana 3000 충돌 (`port ... in use`) | PC에 **호스트 Grafana**가 깔려 3000 점유. 관리자 PowerShell: `Stop-Service Grafana; Set-Service Grafana -StartupType Manual` |
| k6 `couldn't be found on local disk` (Git Bash) | Git Bash가 `/scripts/..` 경로를 윈도우 경로로 변환. 명령 앞에 `MSYS_NO_PATHCONV=1` 붙이기 |
| k6 `Unexpected token ...` / `?.` | 이 k6는 **객체 스프레드(`...`)·옵셔널체이닝(`?.`) 미지원**. 시나리오에서 쓰지 말 것 |

---

## 7단계 프로세스 (내 도메인에 적용)

**1~5는 모든 도메인 공통, 6단계만 도메인 유형별로 갈립니다.**

### 1단계 — 병목 후보 찾기 (가설)
**왜**: 막 부하 주면 결과를 해석 못 합니다. "여기가 느릴 것이다"라는 **가설**이 있어야 측정이 검증이 됩니다.

- 내 도메인을 유형 분류: **조회/집계형**(인덱스·N+1) / **외부연동·스트리밍형**(외부지연·동시성) / **쓰기·트랜잭션형**(락·커넥션) / **단순 CRUD**(병목 거의 없음).
- 의심 API와 이유를 표로. (단순 CRUD면 "병목 없음" 결론도 정당합니다 — 억지 최적화 금지)
- 📄 산출물: `monitoring-local/docs/domains/<도메인>/bottleneck-hypothesis.md`

### 2단계 — 메트릭/로그 심기
**왜**: 자동 HTTP 메트릭은 "요청 전체 시간"만 압니다. **내부 어느 구간이 느린지**는 내가 직접 심어야 보입니다. → 상세: [`metrics-and-loki-guide.md`](metrics-and-loki-guide.md)

- 커스텀 메트릭 이름은 `<도메인>_...` prefix. 의심 구간을 Timer로 감싸기.
- 구조화 로그 `event=<도메인>_<동사> ... durationMs=` 남기기.
- 📄 산출물: `.../metrics.md`

### 3단계 — 성공 기준 정하기
**왜**: "빠르면 성공"은 기준이 아닙니다. **트래픽 조건 + 측정 기간 + p95/실패율**을 같이 적어야 합격/불합격을 판단할 수 있습니다.

- 예: "VU 50, 5분, 조회 API p95 < 500ms, 실패율 < 1%". (유형별 기준표는 컨벤션 참고)

### 4단계 — k6 시나리오 작성
**왜**: 시나리오 = 운영 상황을 단순화한 모델. `monitoring-local/k6/scripts/<도메인>/` 에 작성.

- `01-email-check.js` 또는 [`example-read-list.md`](example-read-list.md) 복제.
- 인증 필요 API면 `lib/auth.js`의 `login()`을 `setup()`에서 호출.
- `tags: { type: "..." }` 로 API 구분 → threshold/대시보드에서 분리됨.

### 5단계 — 시드 + baseline 측정
**왜**: N+1·집계 병목은 **데이터가 많아야** 드러납니다. 방/행 몇 개로는 안 보입니다.

- loadtest DB는 매 부팅 비워지므로(`ddl-auto: create`) **시드가 필요**합니다. (예시 참고)
- baseline 실행:
  ```bash
  MSYS_NO_PATHCONV=1 docker compose run --rm -e RESULT_NAME=<도메인>-before \
    k6 run -o experimental-prometheus-rw /scripts/<도메인>/01-xxx.js
  ```
- 📄 산출물: `monitoring-local/k6/results/<도메인>-before-summary.md`

### 6단계 — 병목 분석 (★유형별)
**왜**: k6 숫자만으로 원인을 확정하지 않습니다. **k6(p95) + Prometheus(내부 수치) + Loki(로그)** 를 교차해 좁힙니다.

- Grafana **"LMS 도메인 대시보드"** → 상단 도메인 변수를 **내 도메인**으로.
- "HTTP p95 ↑ + 내 커스텀 timer ↑ + Loki durationMs ↑" 가 같이 움직이면 그 구간이 범인.
- 유형별 처리: 조회형→인덱스/쿼리/batch, 외부연동→커넥션풀·타임아웃 한계측정, 쓰기형→락·커넥션풀.

### 7단계 — 최적화 적용 → 전후 비교
**왜**: 최적화는 "느낌"이 아니라 **before/after 숫자**로 증명합니다.

- 최적화 적용 후 같은 스크립트를 `RESULT_NAME=<도메인>-after` 로 재실행.
- 📄 산출물: `.../compare.md` (before/after p95·waiting·커스텀 timer·실패율 표 + 결론)

---

## 내가 남길 산출물 (Definition of Done)

```
monitoring-local/docs/domains/<도메인>/
 ├─ bottleneck-hypothesis.md   # 1단계 가설표
 ├─ metrics.md                 # 2단계 심은 메트릭/로그
 └─ compare.md                 # 6·7단계 전후 비교 + 결론
monitoring-local/k6/scripts/<도메인>/*.js    # 4단계 시나리오
monitoring-local/k6/results/*-summary.md     # 5·7단계 결과(자동 저장)
```

> 단순 CRUD형이면 compare 대신 "병목 없음, baseline이 기준 충족" 결론으로 갈음 가능.

---

## 다음 읽을 것

1. 메트릭/Loki를 **왜·어떻게 심는지** 막히면 → [`metrics-and-loki-guide.md`](metrics-and-loki-guide.md)
2. 조회 도메인 **처음부터 끝까지** 따라하기 → [`example-read-list.md`](example-read-list.md)
