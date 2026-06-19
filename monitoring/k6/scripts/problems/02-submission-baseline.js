import http from "k6/http";
import { check, sleep } from "k6";
import { BASE_URL, randomSleep } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

const PROBLEM_ID = __ENV.PROBLEM_ID || "5001";
const SUBMISSION_CODE = __ENV.SUBMISSION_CODE || "result = None\n";

export const options = {
    stages: [
        { duration: "20s", target: 50 },
        { duration: "40s", target: 50 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:submit}": ["p(95)<2000", "p(99)<5000"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
    return { token: login() };
}

export default function (data) {
    const params = authCookies(data.token);
    params.headers = { "Content-Type": "application/json" };
    params.tags = {
        type: "submit",
        api: "POST /problems/{problemId}/submissions",
    };

    const body = JSON.stringify({
        code: SUBMISSION_CODE,
    });

    const res = http.post(
        `${BASE_URL}/api/v1/problems/${PROBLEM_ID}/submissions`,
        body,
        params
    );

    check(res, {
        "status is 200": (r) => r.status === 200,
        "has submissionId": (r) => r.json("data.submissionId") !== undefined,
        "has executionStatus": (r) => r.json("data.executionStatus") !== undefined,
    });

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("submission-baseline");
