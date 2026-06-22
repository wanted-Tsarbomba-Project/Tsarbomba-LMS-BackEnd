# 부하 조건 표준 (Load Profiles) — 팀 공용

> **해석 규칙**(무엇을 합격으로 보나)은 [`CONVENTION.md §0`](CONVENTION.md). **실행 순서**는 [`PROCESS.md`](PROCESS.md).
> 이 문서는 **"어떤 부하를 거느냐"의 표준 프로파일**이다. 팀원은 자기 도메인 스크립트에 아래 프로파일을 복사해 `type` 태그·대상 API만 바꿔 쓴다.
> **부하 숫자(VU·stages)를 임의로 찍지 않는다 — 아래 표준에서 고른다.**

---

## 0. 기준 앵커 — worst-case = 동시 30

우리 수용 기준의 최악 상황은 **실환경 30명이 SSE 채팅 + 코드 실행을 동시에** 무는 상태다. **모든 프로파일의 목표치는 이 `30`에서 파생**한다.

- `TARGET=30` 이 기본. `__ENV.TARGET`으로 덮어쓴다.
- **다운스트림(Gemini·Cloud Run)은 부하 시 목 처리** (`MOCK=on`) — 외부 비용·변동 제거. (CONVENTION §0, 청사진 합의)

---

## 1. 표준 프로파일 5종

| # | 프로파일 | 목적 | VU / 모델 | 게이트(하드) | 환경 |
|---|----------|------|-----------|--------------|------|
| ① | **smoke** | 기능·스크립트 동작 확인 | 1 VU, 5 iter | check 통과, 에러 0 | 로컬 (PR 전 필수) |
| ② | **code-perf** | 코드 직렬 지연 before/after % | 1 VU, 500 iter, **목** | 회귀 없음(상대 p50) | 로컬 |
| ③ | **baseline** | worst-case 부하서 버티나 + SLO | ramp→30, hold 3m | 에러<1%, check | 로컬(버티나) / EC2(SLO) |
| ④ | **stress** | 병목 "위치"(무릎) 탐색 | 계단 20→100 | 에러<5%(관찰 위주) | 로컬·EC2 |
| ⑤ | **worst-case** | 30 SSE + 30 제출 동시 | 2 시나리오 병렬, **목** | 에러<1%, check | 로컬·EC2 |
| ⑥ | **demo-제약** | 풀 고갈 "눈으로" 시연 (교육) | ramp→300, Hikari 10 | 없음(관찰 전용) | 로컬 |

> ②는 **latency 절대값을 게이트로 쓰지 않는다**(환경 종속, §0.2). ③의 latency SLO는 **EC2에서만 하드**, 로컬은 advisory.
> ⑥은 **수용 판정이 아니다 — 교육/시연용**(§0.2). 배포값(Hikari 10)을 **그대로 두고 부하만 키운다** → §0.3 위반 아님(풀을 줄이는 게 아님).

---

## 1.1 각 프로파일이 답하는 질문 — 인사이트로 골라라

**"VU 숫자"가 아니라 "내가 뭘 알고 싶나"로 고른다.** 본인이 부하 숫자를 정하지 말고, 아래 질문 중 자기 것을 골라 해당 프로파일을 복붙한다.

- **① smoke** → *"내 시나리오·인증이 깨지진 않았나?"* — 성능 인사이트 0. 테스트가 유효한지 전제 확인.
- **② code-perf** → *"내 코드 변경이 단일 요청을 빠르게/느리게 했나?"* — 경합 없는 순수 코드 경로 지연 %(알고리즘·쿼리·직렬화). 동시성 문제는 안 보임.
- **③ baseline** → *"목표 부하(30)에서 에러 없이 SLO를 지키나?"* — 약속한 worst-case 통과 여부 + 정상상태 p95.
- **④ stress** → *"몇 명에서, 무엇이 먼저 터지나?"* — 포화점(무릎) 위치 + 1순위 병목 자원(CPU/Hikari/스레드).
- **⑤ worst-case** → *"SSE와 제출이 동시에 자원 다툴 때 버티나?"* — 단일 경로엔 안 보이는 복합 경합(스레드·메모리·커넥션).
- **⑥ demo-제약** → *"제약(풀 10)에 닿으면 곱게 무너지나, 폭삭 무너지나?"* — 고갈 거동(큐잉→타임아웃→거절), graceful degradation. 교육·시연.

---

## 2. 복붙용 k6 options

### ① smoke
```js
export const options = {
  vus: 1, iterations: 5,
  thresholds: { http_req_failed: ['rate==0'], checks: ['rate==1.00'] },
};
```

### ② code-perf (단일 검증, before/after %)
```js
export const options = {
  scenarios: { code_perf: { executor: 'per-vu-iterations', vus: 1, iterations: 500, maxDuration: '10m' } },
  thresholds: { http_req_failed: ['rate<0.01'] },   // 버그 게이트만
  summaryTrendStats: ['min', 'med', 'p(95)', 'max'],
};
// 실행: -e MOCK=on -e RESULT_NAME=<기능>-before  → 코드 수정 → -e RESULT_NAME=<기능>-after
// 비교: results/*-summary.md 의 med(p50). 첫 ~20회는 워밍업이라 해석서 제외.
```

### ③ baseline (worst-case 30, 정상상태)
```js
const TARGET = Number(__ENV.TARGET || 30);
export const options = {
  stages: [
    { duration: '30s', target: TARGET },  // 점진 워밍
    { duration: '3m',  target: TARGET },  // 정상상태 = 측정 구간 (JIT/풀 워밍 + p95 표본 충분)
    { duration: '30s', target: 0 },
  ],
  thresholds: buildThresholds(),          // §3 게이트 표현 패턴
};
```

### ④ stress (무릎 탐색)
```js
export const options = {
  stages: [
    { duration: '30s', target: 20 },
    { duration: '30s', target: 40 },
    { duration: '30s', target: 60 },
    { duration: '30s', target: 80 },
    { duration: '30s', target: 100 },     // 깨질 때까지 계단식
    { duration: '20s', target: 0 },
  ],
  thresholds: { http_req_failed: ['rate<0.05'] },  // 관찰 위주(느슨)
};
// 무릎 = RPS 평평 + latency 꺾임 + 에러 시작. Grafana에서 관찰해 그 VU를 기록.
// 그 순간 서버 메트릭(CPU/Hikari active/스레드)으로 "무엇이" 막는지 특정.
```

### ⑤ worst-case (30 SSE + 30 제출 동시)
```js
const TARGET = Number(__ENV.TARGET || 30);
export const options = {
  scenarios: {
    sse:    { executor: 'constant-vus', vus: TARGET, duration: '3m', exec: 'sseFlow' },
    submit: { executor: 'constant-vus', vus: TARGET, duration: '3m', exec: 'submitFlow' },
  },
  thresholds: { http_req_failed: ['rate<0.01'] },
};
export function sseFlow()    { /* SSE 채팅 — Gemini 목(MOCK=on) */ }
export function submitFlow() { /* 코드 제출 — 러너 호출 */ }
```
> 모델링 주: "30명이 둘을 동시에"를 **SSE 30 VU + 제출 30 VU 두 풀(총 60)**로 근사한다. 서버가 보는 동시성(SSE 30 + 제출 30)은 동일하므로 자원 경합 측정 목적엔 정확하다.

---

### ⑥ demo-제약 (풀 고갈 눈으로 보기 — 교육/시연 전용, 로컬)

> ⚠️ **수용 판정 아님.** 배포 제약(Hikari 10)에서 코드가 풀 고갈로 힘들어하는 모습을 **시각화**하는 데모다. 여기 나온 VU 숫자는 **노트북 의존이라 수용치로 인용 금지**(§0.2). §0.3과 충돌 없음 — 풀을 줄이는 게 아니라 **배포값(Hikari 10) 그대로** 두고 부하만 키운다.

```js
const PEAK = Number(__ENV.PEAK || 300);
export const options = {
  scenarios: { demo: { executor: 'ramping-vus', startVUs: 0, stages: [
    { duration: '1m',  target: PEAK },     // 점진 증가 → 고갈을 "서서히" 보여줌
    { duration: '1m',  target: PEAK },
    { duration: '20s', target: 0 },
  ] } },
  thresholds: {},                          // 게이트 없음 — 순수 관찰
  summaryTrendStats: ['med', 'p(95)', 'p(99)', 'max'],
};
// 대상: DB 무거운 엔드포인트(조회/제출). Hikari 10이 천장.
// Grafana: hikaricp_connections_pending ↑ · active=10 고정 · latency 폭증 · connection-timeout 에러 등장.
```

**왜 1만이 아니라 ~300인가:**
- Hikari 10이라 **동시 ~10에서 이미 풀 포화** → 고갈 현상은 수십~수백 VU에서 다 드러난다. 1만은 새 정보 0.
- 수백 VU 넘으면 **connection-refused 벽**(즉시 에러)만 쌓여 "서서히 힘들어지는" 그림이 사라진다.
- k6 자체가 1만 VU를 한 노트북서 깨끗이 못 만든다 → **생성기가 먼저 병목**(가짜 천장). 수백~1천이 로컬 현실 상한.
- 더 보고 싶으면 `-e PEAK=500`. 그 이상은 무의미.

> 선택: 스레드 고갈까지 보려면 **Tomcat `threads.max=10`을 데모 프로파일에만** 임시 적용. 단 **배포값(200) 아님 — 데모 한정 인위 제약**임을 명시. 배포/검증엔 절대 반영 금지.

**"1만 유저 동시 사용" 느낌을 내려면 — VU가 아니라 도착률(RPS)로:**

"동시 유저 N명" ≠ VU N개. 실제 유저는 행동 사이에 think-time(20~30초)이 있다. **N명을 도착률로 환산**한다:

```
1만 유저 × (행동 1회 / 25초 think-time) ≈ 400 req/s
```

```js
export const options = {
  scenarios: { mass: { executor: 'constant-arrival-rate',
    rate: Number(__ENV.RPS || 400), timeUnit: '1s', duration: '2m',
    preAllocatedVUs: 200, maxVUs: 1000 } },
  thresholds: {},   // 관찰 전용
};
```
> 이게 "1만 유저 규모"의 **정직한 로컬 표현**이다. VU 1만은 생성기(k6)가 먼저 죽어 가짜 천장(위 설명). 도착률은 같은 규모를 **노트북이 감당 가능한 형태**로 준다. 단 여전히 **수용치 아님 — 시연/관찰용**(Hikari 10이라 즉시 포화). 규모 조절은 `-e RPS=...`.

---

## 3. 게이트 표현 패턴 (§0 규칙을 코드로)

에러·check는 **항상 하드**, latency는 **로컬 advisory / EC2 하드**. 환경으로 가른다:

```js
function buildThresholds() {
  const ENV = __ENV.ENV || 'local';
  const base = {
    http_req_failed: ['rate<0.01'],   // 하드 (환경 독립, §0.1)
    checks: ['rate>0.99'],            // 하드
  };
  if (ENV === 'ec2') {                 // 절대 SLO는 EC2에서만 하드 게이트 (§0.2)
    base['http_req_duration{type:read}']   = ['p(95)<500'];
    base['http_req_duration{type:submit}'] = ['p(95)<2000', 'p(99)<5000'];
    base['http_req_duration{type:stream}'] = ['p(95)<1000'];  // SSE 첫 토큰(목 기준)
  }
  return base;
}
```
실행: 로컬은 그냥, EC2 타깃은 `-e ENV=ec2`.

---

## 4. 도메인 유형 → 프로파일 매핑

자기 도메인을 [`PROCESS.md` 1단계](PROCESS.md)에서 분류한 뒤, 돌릴 프로파일을 고른다:

| 도메인 유형 | 필수 | 선택 |
|-------------|------|------|
| 조회/집계형 (N+1·인덱스) | ① smoke, ③ baseline | ② code-perf(최적화 시), ④ stress |
| 쓰기/트랜잭션형 (락·풀) | ① smoke, ③ baseline, ④ stress | ② code-perf |
| 외부연동/스트리밍형 (SSE·러너) | ① smoke, ⑤ worst-case | ④ stress(한계 측정) |
| 단순 CRUD형 | ① smoke, ③ baseline | — ("병목 없음" 정당) |

---

## 5. 실행 매트릭스 — 언제 무엇을

| 시점 | 프로파일 | 환경 |
|------|----------|------|
| PR 올리기 전 | ① smoke | 로컬 |
| 코드 최적화 작업 | ② code-perf (before/after) | 로컬 |
| 도메인 완료(DoD) | ③ baseline (+ 유형별 ④/⑤) | 로컬 |
| 시연·학습 (풀 고갈 시각화) | ⑥ demo-제약 | 로컬 |
| **수용 천장·절대 SLO 확정 (리더)** | ③ baseline + ④ stress, `-e ENV=ec2` | **EC2** (micro vs small 비교) |

> 로컬은 "버티나 + 상대 %", **절대 수치(천장·p95)는 EC2 1곳에서만**. (§0.2)

---

## 6. 도메인 owner가 채울 칸

프로파일은 고정, 아래만 도메인별로 채운다:

- **대상 API** (Method, URI) + k6 `type` 태그 (`{type:read}` 등 — §3 게이트와 매칭)
- **§6 유형별 성공 기준** (CONVENTION §6 표에서 자기 API 유형 골라 p95/실패율)
- **시드 데이터량** (`db/seed/` — N+1·집계는 데이터 많아야 드러남, PROCESS 5단계)
- ⑤ 대상이면 **목 플래그**로 외부(Gemini) 차단 확인

---

## 참고
- 해석 규칙: [`CONVENTION.md §0`](CONVENTION.md) · 성공 기준 표: [`CONVENTION.md §6`](CONVENTION.md)
- 실행 7단계: [`PROCESS.md`](PROCESS.md) · 실행 명령: [`../README.md`](../README.md)
