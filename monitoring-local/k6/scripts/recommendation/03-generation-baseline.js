// Recommendation 트랙 B — POST /internal/loadtest/recommendations/generate baseline
//
// 목적: 추천 생성 배치 본체를 cron 대기 없이 실행해 FastAPI 호출/저장 병목을 분리 측정한다.
//   → recommendation_generation_batch_duration, recommendation_generation_external_duration,
//      recommendation_generation_save_duration 과 함께 before/after 비교.
//
// 실행 (monitoring-local/ 에서):
//   docker compose run --rm -e RESULT_NAME=recommendation-generation-before \
//     -e SCALE_USERS=120 \
//     -e LOGIN_EMAIL=admin@test.com -e LOGIN_PASSWORD=Test1234! \
//     k6 run -o experimental-prometheus-rw /scripts/recommendation/03-generation-baseline.js

import http from "k6/http";
import { check, sleep } from "k6";
import { BASE_URL } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

export const options = {
    vus: Number(__ENV.VUS || 1),
    iterations: Number(__ENV.ITERATIONS || 1),
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:recommendation_generation}": ["p(95)<300000"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
    return { token: login() };
}

export default function (data) {
    const scaleUsers = String(__ENV.SCALE_USERS || "120");
    const params = authCookies(data.token);
    params.tags = {
        type: "recommendation_generation",
        api: "POST /internal/loadtest/recommendations/generate",
        scale_users: scaleUsers,
    };

    const res = http.post(`${BASE_URL}/internal/loadtest/recommendations/generate`, null, params);

    check(res, {
        "status is 200": (r) => r.status === 200,
        "trigger completed": (r) => r.json("status") === "completed",
        "generated user count exists": (r) => Number(r.json("generatedUserCount")) >= 0,
    });

    sleep(1);
}

export const handleSummary = createSummaryHandler("recommendation-generation-baseline");
