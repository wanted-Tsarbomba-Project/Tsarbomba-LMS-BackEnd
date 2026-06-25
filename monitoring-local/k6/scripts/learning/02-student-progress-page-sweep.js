// Learning track A - paged sweep for GET /api/v1/courses/{courseId}/users/learning-progress
//
// Purpose:
//   Verify that the paged implementation completes one full sweep across every page
//   of a large dataset.
//   With the default loadtest seed of 10,000 students and server-side page size 20,
//   this script requests page=0..499 once.
//
// Run from monitoring-local/:
//   docker compose run --rm -e LOGIN_EMAIL=learning-loadtest-admin@test.com -e LOGIN_PASSWORD=Test1234! \
//     -e COURSE_ID=2000 -e TOTAL_PAGES=500 -e VUS=30 -e RESULT_NAME=learning-student-progress-paging-10000 \
//     k6 run -o experimental-prometheus-rw /scripts/learning/02-student-progress-page-sweep.js

import http from "k6/http";
import { check, sleep } from "k6";
import exec from "k6/execution";
import { BASE_URL, randomSleep } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

const COURSE_ID = __ENV.COURSE_ID || "2000";
const TOTAL_PAGES = Number(__ENV.TOTAL_PAGES || "500");
const VUS = Number(__ENV.VUS || "30");
const MAX_DURATION = __ENV.MAX_DURATION || "10m";

export const options = {
    scenarios: {
        page_sweep_once: {
            executor: "shared-iterations",
            vus: VUS,
            iterations: TOTAL_PAGES,
            maxDuration: MAX_DURATION,
        },
    },
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:student-progress-page-sweep}": ["p(95)<800"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
    return { token: login() };
}

export default function (data) {
    const page = exec.scenario.iterationInTest % TOTAL_PAGES;
    const params = authCookies(data.token);
    params.tags = {
        type: "student-progress-page-sweep",
        api: "GET /courses/{courseId}/users/learning-progress",
    };

    const res = http.get(
        `${BASE_URL}/api/v1/courses/${COURSE_ID}/users/learning-progress?page=${page}`,
        params
    );

    check(res, {
        "status is 200": (r) => r.status === 200,
        "has page content array": (r) => Array.isArray(r.json("data.content")),
        "page matches request": (r) => r.json("data.page") === page,
        "page size is 20": (r) => r.json("data.size") === 20,
    });

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("learning-student-progress-page-sweep", "learning");
