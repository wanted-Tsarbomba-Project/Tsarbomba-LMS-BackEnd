import http from "k6/http";
import { check, sleep } from "k6";
import { BASE_URL, randomEmail, randomSleep } from "../../lib/config.js";
import { createSummaryHandler } from "../../lib/summary.js";

// ── 팀 Load Profiles 표준 ──
// PROFILE=smoke | baseline | stress   (기본 baseline)
// TARGET=30 (worst-case 앵커, __ENV.TARGET 으로 덮어씀)
// ENV=ec2 면 latency 절대 SLO 를 하드 게이트로 (§0.2)
const PROFILE = (__ENV.PROFILE || "baseline").toLowerCase();
const TARGET = Number(__ENV.TARGET || 30);

function buildThresholds() {
    const ENV = __ENV.ENV || "local";
    const base = {
        http_req_failed: ["rate<0.01"], // 하드 (환경 독립)
        checks: ["rate>0.99"],          // 하드
    };
    if (ENV === "ec2") {
        base["http_req_duration{type:read}"] = ["p(95)<500"]; // EC2 에서만 하드
    }
    return base;
}

const PROFILES = {
    // ① smoke — 기능·스크립트 동작 확인
    smoke: {
        vus: 1,
        iterations: 5,
        thresholds: { http_req_failed: ["rate==0"], checks: ["rate==1.00"] },
    },
    // ③ baseline — worst-case 30 정상상태 + SLO
    baseline: {
        stages: [
            { duration: "30s", target: TARGET }, // 점진 워밍
            { duration: "3m", target: TARGET },  // 정상상태 = 측정 구간
            { duration: "30s", target: 0 },
        ],
        thresholds: buildThresholds(),
    },
    // ④ stress — 무릎(병목 위치) 탐색
    stress: {
        stages: [
            { duration: "30s", target: 20 },
            { duration: "30s", target: 40 },
            { duration: "30s", target: 60 },
            { duration: "30s", target: 80 },
            { duration: "30s", target: 100 },
            { duration: "20s", target: 0 },
        ],
        thresholds: { http_req_failed: ["rate<0.05"] }, // 관찰 위주(느슨)
    },
};

const selectedProfile = PROFILES[PROFILE] || PROFILES.baseline;
export const options = Object.assign({}, selectedProfile, {
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
});

export default function () {
    const email = randomEmail();
    const res = http.get(`${BASE_URL}/api/v1/auth/check/email?email=${email}`, {
        tags: { type: "read", api: "GET /auth/check/email" }, // §3 게이트와 매칭
    });

    check(res, {
        "status is 200": (r) => r.status === 200,
        "has available field": (r) => r.json("data.available") !== undefined,
    });

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler(`01-email-check-${PROFILE}`);
