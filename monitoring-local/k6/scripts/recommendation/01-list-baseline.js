// Recommendation 트랙 A — GET /api/v1/recommendations/problem-sets/me 단일 baseline
//
// 목적: 추천 목록 조회의 숨김 확인 + ACTIVE 추천 native query 비용을 baseline 으로 박는다.
//   → 후속 인덱스/쿼리 최적화 후 같은 스크립트로 before/after 비교.
//
// ⚠️ 전제 (5단계 시드):
//   - loadtest DB 에 로그인 계정(auth.js 의 LOGIN_EMAIL/PASSWORD)이 있어야 한다.
//   - 그 계정에 ACTIVE 추천 row 와 ACTIVE 문제 세트/카테고리 데이터가 있어야 한다.
//   - hide-today 상태면 추천 query 를 건너뛰므로 baseline 전에 숨김 row 를 비활성/만료 상태로 둔다.
//
// 실행 (monitoring-local/ 에서):
//   docker compose run --rm -e RESULT_NAME=recommendation-list-before \
//     -e LOGIN_EMAIL=u01@test.com -e LOGIN_PASSWORD=Test1234! \
//     k6 run -o experimental-prometheus-rw /scripts/recommendation/01-list-baseline.js

import http from "k6/http";
import { check, sleep } from "k6";
import { BASE_URL, randomSleep } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

export const options = {
    stages: [
        { duration: "20s", target: 50 },
        { duration: "40s", target: 50 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:list}": ["p(95)<500"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
    return { token: login() };
}

export default function (data) {
    const params = authCookies(data.token);
    params.tags = { type: "list", api: "GET /recommendations/problem-sets/me" };

    const res = http.get(`${BASE_URL}/api/v1/recommendations/problem-sets/me?limit=3`, params);

    check(res, {
        "status is 200": (r) => r.status === 200,
        "data exists": (r) => r.json("data") !== null,
        "not hidden for query baseline": (r) => r.json("data.hidden") === false,
        "problemSets is array": (r) => Array.isArray(r.json("data.problemSets")),
    });

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("recommendation-list-baseline");
