import http from "k6/http";
import { check, sleep } from "k6";
import { BASE_URL, randomInt, randomSleep } from "../../lib/config.js";
import { createSummaryHandler } from "../../lib/summary.js";

// ── 팀 Load Profiles 표준 (01-email-check 과 동일 구조) ──
const PROFILE = (__ENV.PROFILE || "baseline").toLowerCase();
const TARGET = Number(__ENV.TARGET || 30);

function buildThresholds() {
    const ENV = __ENV.ENV || "local";
    const base = {
        http_req_failed: ["rate<0.01"],
        checks: ["rate>0.99"],
    };
    if (ENV === "ec2") {
        base["http_req_duration{type:read}"] = ["p(95)<500"];
    }
    return base;
}

const PROFILES = {
    smoke: {
        vus: 1,
        iterations: 5,
        thresholds: { http_req_failed: ["rate==0"], checks: ["rate==1.00"] },
    },
    baseline: {
        stages: [
            { duration: "30s", target: TARGET },
            { duration: "3m", target: TARGET },
            { duration: "30s", target: 0 },
        ],
        thresholds: buildThresholds(),
    },
    stress: {
        stages: [
            { duration: "30s", target: 20 },
            { duration: "30s", target: 40 },
            { duration: "30s", target: 60 },
            { duration: "30s", target: 80 },
            { duration: "30s", target: 100 },
            { duration: "20s", target: 0 },
        ],
        thresholds: { http_req_failed: ["rate<0.05"] },
    },
};

const selectedProfile = PROFILES[PROFILE] || PROFILES.baseline;
export const options = Object.assign({}, selectedProfile, {
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
});

export default function () {
    const nickname = `loadtest_${randomInt(1, 1000000)}`;
    const res = http.get(`${BASE_URL}/api/v1/auth/check/nickname?nickname=${nickname}`, {
        tags: { type: "read", api: "GET /auth/check/nickname" },
    });

    check(res, {
        "status is 200": (r) => r.status === 200,
        "has available field": (r) => r.json("data.available") !== undefined,
    });

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler(`02-nickname-check-${PROFILE}`);
