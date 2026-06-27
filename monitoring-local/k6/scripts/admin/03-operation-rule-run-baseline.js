// Admin 가설 #4 — POST /internal/loadtest/admin/operation-rules/run baseline
//
// 목적: 운영 자동화 스케줄러 본체를 cron 대기 없이 실행해 룰별 detect/upsert 병목을 측정한다.
//   → admin_operation_rule_run_duration, admin_operation_rule_detect_duration,
//      admin_operation_alert_upsert_duration 과 함께 before/after 비교.
//
// 실행 (monitoring-local/ 에서):
//   docker compose run --rm -e RESULT_NAME=admin-operation-rule-run-before \
//     -e LOGIN_EMAIL=admin@test.com -e LOGIN_PASSWORD=Test1234! \
//     k6 run -o experimental-prometheus-rw /scripts/admin/03-operation-rule-run-baseline.js

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
        "http_req_duration{type:operation_rule_run}": ["p(95)<10000"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
    return { token: login() };
}

export default function (data) {
    const params = authCookies(data.token);
    params.tags = {
        type: "operation_rule_run",
        api: "POST /internal/loadtest/admin/operation-rules/run",
    };

    const res = http.post(`${BASE_URL}/internal/loadtest/admin/operation-rules/run`, null, params);

    check(res, {
        "status is 200": (r) => r.status === 200,
        "trigger completed": (r) => r.json("status") === "completed",
        "duration exists": (r) => Number(r.json("durationMs")) >= 0,
    });

    sleep(1);
}

export const handleSummary = createSummaryHandler("admin-operation-rule-run-baseline");
