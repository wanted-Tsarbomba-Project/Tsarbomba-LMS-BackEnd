// Recommendation 가설 #4 — POST /api/v1/recommendations/problem-sets/hide-today baseline
//
// 목적: 추천 숨김 upsert 성능을 추천 목록 조회와 분리해 측정한다.
//   → 반복 실행 시 최초 1회는 insert, 이후는 update 경로를 탄다.
//
// 실행 (monitoring/ 에서):
//   docker compose run --rm -e RESULT_NAME=recommendation-hide-before \
//     -e LOGIN_EMAIL=u01@test.com -e LOGIN_PASSWORD=Test1234! \
//     k6 run -o experimental-prometheus-rw /scripts/recommendation/02-hide-today-baseline.js

import http from "k6/http";
import { check, sleep } from "k6";
import { BASE_URL, randomSleep } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

export const options = {
    stages: [
        { duration: "20s", target: 30 },
        { duration: "40s", target: 30 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:hide}": ["p(95)<300"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
    return { token: login() };
}

export default function (data) {
    const params = authCookies(data.token);
    params.tags = { type: "hide", api: "POST /recommendations/problem-sets/hide-today" };

    const res = http.post(`${BASE_URL}/api/v1/recommendations/problem-sets/hide-today`, null, params);

    check(res, {
        "status is 200": (r) => r.status === 200,
        "hidden is true": (r) => r.json("data.hidden") === true,
        "hiddenUntil exists": (r) => r.json("data.hiddenUntil") !== null,
    });

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("recommendation-hide-today-baseline");
