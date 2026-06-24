// Admin 가설 #3 — GET /api/v1/admin/operation-alerts 목록 조회 baseline
//
// 목적: 운영 알림 목록의 필터 + 페이징 + 정렬 query 비용을 단독으로 측정한다.
//   → admin_operation_alert_list_query_duration 과 함께 보면 목록 query/정렬 병목을 분리할 수 있다.
//
// 실행 (monitoring-local/ 에서):
//   docker compose run --rm -e RESULT_NAME=admin-alert-list-before \
//     -e LOGIN_EMAIL=admin@test.com -e LOGIN_PASSWORD=Test1234! \
//     k6 run -o experimental-prometheus-rw /scripts/admin/01-alert-list-baseline.js

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
        "http_req_duration{type:list}": ["p(95)<800"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
    return { token: login() };
}

export default function (data) {
    const params = authCookies(data.token);
    params.tags = { type: "list", api: "GET /admin/operation-alerts" };

    const res = http.get(`${BASE_URL}/api/v1/admin/operation-alerts?page=0&size=20`, params);

    check(res, {
        "status is 200": (r) => r.status === 200,
        "content is array": (r) => Array.isArray(r.json("data.content")),
        "has alert seed": (r) => {
            const content = r.json("data.content");
            return Array.isArray(content) && content.length > 0;
        },
    });

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("admin-alert-list-baseline");
