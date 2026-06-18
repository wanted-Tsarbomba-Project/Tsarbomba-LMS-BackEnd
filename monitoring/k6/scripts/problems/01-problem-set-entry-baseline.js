import http from "k6/http";
import { check, sleep } from "k6";
import { BASE_URL, randomSleep } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

const PROBLEM_SET_ID = __ENV.PROBLEM_SET_ID || "4001";

export const options = {
    stages: [
        { duration: "20s", target: 50 },
        { duration: "40s", target: 50 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:entry}": ["p(95)<800", "p(99)<2000"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
    return { token: login() };
}

export default function (data) {
    const params = authCookies(data.token);
    params.tags = {
        type: "entry",
        api: "GET /problem-sets/{problemSetId}",
    };

    const res = http.get(
        `${BASE_URL}/api/v1/problem-sets/${PROBLEM_SET_ID}`,
        params
    );

    check(res, {
        "status is 200": (r) => r.status === 200,
        "has problemSetId": (r) => r.json("data.problemSetId") !== undefined,
        "has problems array": (r) => Array.isArray(r.json("data.problems")),
    });

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("problem-set-entry-baseline");
