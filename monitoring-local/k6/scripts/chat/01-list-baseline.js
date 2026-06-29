// 챗봇 트랙 A — GET /api/v1/chat/list 단일 baseline
//
// 목적: 채팅방 목록 조회의 N+1(방마다 problem 제목 2회 조회) 비용을 baseline 으로 박는다.
//   → 인덱스가 아니라 쿼리/페치 리팩토링으로 해소 후 같은 스크립트로 전후 비교.
//
// ⚠️ 전제 (5단계 시드):
//   - loadtest DB 에 로그인 계정(auth.js 의 LOGIN_EMAIL/PASSWORD)이 있어야 한다.
//   - 그 계정이 "방을 많이" 가지고 있어야 N+1 이 드러난다(방 5개로는 안 보임).
//     각 방은 problemSetId/problemId 가 채워져야 제목 fanout 2회가 실제로 발생.
//
// 실행 (monitoring-local/ 에서):
//   docker compose run --rm -e RESULT_NAME=chat-list-before \
//     k6 run -o experimental-prometheus-rw /scripts/chat/01-list-baseline.js

import http from "k6/http";
import { check, sleep } from "k6";
import { BASE_URL, randomSleep } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

export const options = {
    // 0 → 50명까지 올렸다 유지 후 종료 (auth baseline 과 동일 형태)
    stages: [
        { duration: "20s", target: 50 },
        { duration: "40s", target: 50 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],                  // 실패율 1% 미만
        "http_req_duration{type:list}": ["p(95)<500"],   // 조회형 합격 기준 (CONVENTION §6)
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

// 부하 시작 전 1회만 로그인 → 로그인 부하가 본 측정에 안 섞인다.
export function setup() {
    return { token: login() };
}

export default function (data) {
    // k6 babel 이 객체 스프레드(...)를 안 받아서, 헬퍼 반환 객체에 tags 만 붙여 쓴다.
    const params = authCookies(data.token);
    params.tags = { type: "list", api: "GET /chat/list" };   // threshold·Grafana 분리용 태그

    const res = http.get(`${BASE_URL}/api/v1/chat/list`, params);

    check(res, {
        "status is 200": (r) => r.status === 200,
        "has data array": (r) => Array.isArray(r.json("data")),
    });

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("chat-list-baseline");
