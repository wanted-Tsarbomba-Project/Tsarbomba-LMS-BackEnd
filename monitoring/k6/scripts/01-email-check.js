import http from "k6/http";
import { check, sleep } from "k6";
import { BASE_URL, randomEmail, randomSleep } from "../lib/config.js";
import { createSummaryHandler } from "../lib/summary.js";

export const options = {
    // 부하 단계: 0 → 50명까지 서서히 올렸다 유지 후 종료
    stages: [
        { duration: "20s", target: 50 },  // 20초간 50명까지 증가
        { duration: "40s", target: 50 },  // 40초간 50명 유지
        { duration: "10s", target: 0 },   // 10초간 0으로 감소
    ],
    // 합격 기준
    thresholds: {
        http_req_failed: ["rate<0.01"],          // 실패율 1% 미만
        http_req_duration: ["p(95)<500"],        // p95 응답시간 500ms 미만
    },
    // p99까지 결과에 포함
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export default function () {
    const email = randomEmail();
    const res = http.get(`${BASE_URL}/api/v1/auth/check/email?email=${email}`, {
        tags: { api: "GET /auth/check/email" },
    });

    check(res, {
        "status is 200": (r) => r.status === 200,
        "has available field": (r) => r.json("data.available") !== undefined,
    });

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("01-email-check");
