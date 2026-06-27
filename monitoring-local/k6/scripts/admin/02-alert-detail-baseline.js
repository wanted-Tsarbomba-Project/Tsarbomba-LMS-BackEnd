// Admin 가설 #4 — GET /api/v1/admin/operation-alerts/{operationAlertId} 상세 조회 baseline
//
// 목적: 운영 알림 상세 기본 조회 + target detail 조합 비용을 단독으로 측정한다.
//   → admin_operation_alert_detail_query_duration 과
//      admin_operation_alert_target_detail_duration{targetType=...} 을 함께 본다.
//
// ⚠️ OPERATION_ALERT_ID 는 상세 조회 대상 알림 ID 다. loadtest seed 기본값은 1번부터 생성된다.
//
// 실행 (monitoring-local/ 에서):
//   docker compose run --rm -e RESULT_NAME=admin-alert-detail-before \
//     -e LOGIN_EMAIL=admin@test.com -e LOGIN_PASSWORD=Test1234! \
//     -e OPERATION_ALERT_ID=1 \
//     k6 run -o experimental-prometheus-rw /scripts/admin/02-alert-detail-baseline.js

import http from "k6/http";
import { check, sleep } from "k6";
import { BASE_URL, randomSleep } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

const OPERATION_ALERT_ID = __ENV.OPERATION_ALERT_ID || "1";

export const options = {
    stages: [
        { duration: "20s", target: 50 },
        { duration: "40s", target: 50 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:detail}": ["p(95)<500"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
    return { token: login() };
}

export default function (data) {
    const params = authCookies(data.token);
    params.tags = { type: "detail", api: "GET /admin/operation-alerts/{id}" };

    const res = http.get(`${BASE_URL}/api/v1/admin/operation-alerts/${OPERATION_ALERT_ID}`, params);

    check(res, {
        "status is 200": (r) => r.status === 200,
        "data exists": (r) => r.json("data") !== null,
        "has target detail": (r) => r.json("data.target") !== null,
    });

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("admin-alert-detail-baseline");
