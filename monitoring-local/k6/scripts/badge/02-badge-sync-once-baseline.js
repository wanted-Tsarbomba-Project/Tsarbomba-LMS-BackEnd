import http from "k6/http";
import { check } from "k6";
import exec from "k6/execution";
import { Rate, Trend } from "k6/metrics";
import { BASE_URL } from "../../lib/config.js";
import { createSummaryHandler } from "../../lib/summary.js";

const USER_ID_START = Number(__ENV.BADGE_SYNC_USER_ID_START || 810001);
const USER_COUNT = Number(__ENV.BADGE_SYNC_USER_COUNT || 300);
const PASSWORD = __ENV.BADGE_SYNC_PASSWORD || "Test1234!";

const BADGE_SYNC_API = "POST /badges/me/sync";

const badgeSyncValidRate = new Rate("badge_sync_valid_rate");
const badgeSyncNewlyEarnedCount = new Trend("badge_sync_newly_earned_count");
const badgeSyncTotalPoint = new Trend("badge_sync_total_point");

export const options = {
    scenarios: {
        badge_sync_once: {
            executor: "shared-iterations",
            vus: Number(__ENV.VUS || 50),
            iterations: USER_COUNT,
            maxDuration: __ENV.MAX_DURATION || "2m",
        },
    },
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:badge_sync_once}": ["p(95)<1000", "p(99)<3000"],
        badge_sync_valid_rate: ["rate>0.99"],
        badge_sync_newly_earned_count: ["avg>50"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
    const tokens = [];

    for (let i = 0; i < USER_COUNT; i += 1) {
        const userId = USER_ID_START + i;
        const email = `badge-sync-loadtest-${userId}@test.com`;
        tokens.push(login(email));
    }

    return { tokens };
}

function login(email) {
    const res = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({ email, password: PASSWORD }),
        {
            headers: { "Content-Type": "application/json" },
            tags: {
                type: "badge_sync_login",
                api: "POST /auth/login",
            },
        }
    );

    if (res.status !== 200) {
        throw new Error(`[auth] login failed email=${email} status=${res.status} body=${res.body}`);
    }

    const cookie = res.cookies.accessToken && res.cookies.accessToken[0];
    if (!cookie || !cookie.value) {
        throw new Error(`[auth] accessToken cookie missing email=${email}`);
    }

    return cookie.value;
}

export default function (data) {
    const index = exec.scenario.iterationInTest;
    const token = data.tokens[index];

    const res = http.post(`${BASE_URL}/api/v1/badges/me/sync`, null, {
        cookies: { accessToken: token },
        tags: {
            type: "badge_sync_once",
            api: BADGE_SYNC_API,
        },
    });

    let result = {};
    if (res.status === 200) {
        result = res.json("data") || {};
    }

    const newlyEarnedBadgeCount = Number(result.newlyEarnedBadgeCount || 0);
    const valid = res.status === 200
        && result.totalPoint !== undefined
        && result.newlyEarnedBadgeCount !== undefined
        && newlyEarnedBadgeCount > 0;

    badgeSyncValidRate.add(valid, { api: BADGE_SYNC_API });

    if (res.status === 200 && result.totalPoint !== undefined) {
        badgeSyncTotalPoint.add(Number(result.totalPoint || 0), { api: BADGE_SYNC_API });
        badgeSyncNewlyEarnedCount.add(newlyEarnedBadgeCount, { api: BADGE_SYNC_API });
    }

    check(res, {
        "badge sync status is 200": (r) => r.status === 200,
        "badge sync has totalPoint": () => result.totalPoint !== undefined,
        "badge sync newly earns badges": () => newlyEarnedBadgeCount > 0,
    });
}

export const handleSummary = createSummaryHandler("badge-sync-once-baseline");
