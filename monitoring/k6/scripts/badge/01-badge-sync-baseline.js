import http from "k6/http";
import { check, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";
import { BASE_URL, randomSleep } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

const BADGE_SYNC_API = "POST /badges/me/sync";
const MY_BADGE_API = "GET /badges/me";

const badgeResponseValidRate = new Rate("badge_response_valid_rate");
const badgeNewlyEarnedCount = new Trend("badge_newly_earned_count");
const badgeOwnedCount = new Trend("badge_owned_count");
const badgeTotalPoint = new Trend("badge_total_point");

export const options = {
    stages: [
        { duration: "20s", target: 50 },
        { duration: "40s", target: 50 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:badge_sync}": ["p(95)<1000", "p(99)<3000"],
        "http_req_duration{type:badge_list}": ["p(95)<800", "p(99)<2000"],
        badge_response_valid_rate: ["rate>0.99"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
    return { token: login() };
}

function syncBadges(token) {
    const params = authCookies(token);
    params.tags = {
        type: "badge_sync",
        api: BADGE_SYNC_API,
    };

    const res = http.post(`${BASE_URL}/api/v1/badges/me/sync`, null, params);

    let data = {};
    if (res.status === 200) {
        data = res.json("data") || {};
    }

    const valid = res.status === 200
        && data.totalPoint !== undefined
        && data.newlyEarnedBadgeCount !== undefined;

    badgeResponseValidRate.add(valid, { api: BADGE_SYNC_API });

    if (valid) {
        badgeTotalPoint.add(Number(data.totalPoint || 0), { api: BADGE_SYNC_API });
        badgeNewlyEarnedCount.add(
            Number(data.newlyEarnedBadgeCount || 0),
            { api: BADGE_SYNC_API }
        );
    }

    check(res, {
        "badge sync status is 200": (r) => r.status === 200,
        "badge sync has totalPoint": () => data.totalPoint !== undefined,
        "badge sync has newlyEarnedBadgeCount": () =>
            data.newlyEarnedBadgeCount !== undefined,
    });
}

function getMyBadges(token) {
    const params = authCookies(token);
    params.tags = {
        type: "badge_list",
        api: MY_BADGE_API,
    };

    const res = http.get(`${BASE_URL}/api/v1/badges/me`, params);

    let badges = [];
    if (res.status === 200) {
        badges = res.json("data") || [];
    }

    const valid = res.status === 200 && Array.isArray(badges);
    badgeResponseValidRate.add(valid, { api: MY_BADGE_API });

    if (valid) {
        badgeOwnedCount.add(badges.length, { api: MY_BADGE_API });
    }

    check(res, {
        "my badge status is 200": (r) => r.status === 200,
        "my badge has array": () => Array.isArray(badges),
    });
}

export default function (data) {
    syncBadges(data.token);
    getMyBadges(data.token);

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("badge-sync-baseline");
