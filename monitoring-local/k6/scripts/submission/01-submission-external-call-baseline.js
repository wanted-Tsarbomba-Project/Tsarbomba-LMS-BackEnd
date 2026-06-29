import http from "k6/http";
import exec from "k6/execution";
import { check } from "k6";
import { Rate, Trend } from "k6/metrics";

import { BASE_URL } from "../../lib/config.js";
import { authCookies, login } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

const PROBLEM_ID = Number(__ENV.PROBLEM_ID || "840001");
const USER_ID_START = Number(__ENV.USER_ID_START || "840001");
const USER_COUNT = Number(__ENV.USER_COUNT || "300");
const PASSWORD = __ENV.PASSWORD || "Test1234!";

const submissionDuration = new Trend(
    "submission_external_call_duration",
    true
);
const submissionValidRate = new Rate(
    "submission_external_call_valid_rate"
);
const submissionFailedRate = new Rate(
    "submission_external_call_failed_rate"
);

export const options = {
    scenarios: {
        submission_external_call: {
            executor: "shared-iterations",
            vus: Number(__ENV.VUS || "50"),
            iterations: Number(__ENV.ITERATIONS || "300"),
            maxDuration: __ENV.MAX_DURATION || "3m",
        },
    },

    thresholds: {
        http_req_failed: ["rate<0.01"],
        submission_external_call_failed_rate: ["rate<0.01"],
        submission_external_call_valid_rate: ["rate>0.99"],
        "http_req_duration{type:submission_external_call}": [
            "p(95)<2000",
        ],
    },

    summaryTrendStats: [
        "avg",
        "med",
        "p(90)",
        "p(95)",
        "p(99)",
        "max",
    ],
};

export function setup() {
    const tokens = [];

    for (let i = 0; i < USER_COUNT; i += 1) {
        const userId = USER_ID_START + i;
        const email =
            "submission-loadtest-" + userId + "@test.com";
        const deviceId =
            "submission-loadtest-device-" + userId;

        const token = login(email, PASSWORD, {
            cookies: {
                deviceId: deviceId,
            },
        });

        tokens.push(token);
    }

    return {
        tokens: tokens,
    };
}

export default function (data) {
    const iterationIndex = exec.scenario.iterationInTest;
    const userIndex = iterationIndex % data.tokens.length;
    const token = data.tokens[userIndex];

    const params = authCookies(token);

    params.headers = {
        "Content-Type": "application/json",
        Accept: "application/json",
    };

    params.tags = {
        type: "submission_external_call",
        api: "POST /problems/{problemId}/submissions",
    };

    // result가 없으므로 Mock Runner는 오답으로 처리합니다.
    // 포인트 및 다음 문제 이벤트를 제외하고 채점 대기와 저장만 측정합니다.
    const payload = JSON.stringify({
        code: "print('submission-loadtest')",
    });

    const response = http.post(
        BASE_URL
        + "/api/v1/problems/"
        + PROBLEM_ID
        + "/submissions",
        payload,
        params
    );

    submissionDuration.add(response.timings.duration, {
        type: "submission_external_call",
    });
    submissionFailedRate.add(response.status !== 200);

    let body = null;

    try {
        body = response.json();
    } catch (e) {
        body = null;
    }

    const hasData =
        body !== null
        && body.data !== null
        && body.data !== undefined;

    const valid = check(response, {
        "status is 200": function (res) {
            return res.status === 200;
        },

        "response has data": function () {
            return hasData;
        },

        "problem id is valid": function () {
            return hasData
                && body.data.problemId === PROBLEM_ID;
        },

        "submission id exists": function () {
            return hasData
                && body.data.submissionId !== null;
        },

        "test case count is 5": function () {
            return hasData
                && body.data.totalTestCount === 5;
        },

        "result is wrong answer": function () {
            return hasData
                && body.data.isCorrect === false
                && body.data.executionStatus === "WRONG_ANSWER";
        },
    });

    submissionValidRate.add(valid);
}

export const handleSummary = createSummaryHandler(
    "submission-external-call-before-transaction-split"
);
