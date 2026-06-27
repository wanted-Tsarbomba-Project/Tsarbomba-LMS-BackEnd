import http from "k6/http";
import { check, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";
import { BASE_URL, randomSleep } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

const PAGE = __ENV.RANKING_PAGE || "0";
const SIZE = __ENV.RANKING_SIZE || "100";

const TOTAL_RANKING_API = "GET /rankings/points";
const WEEKLY_RANKING_API = "GET /rankings/points/weekly";

const rankingBadgeResponseValidRate = new Rate("ranking_badge_response_valid_rate");
const rankingBadgeImageUrlPresentRate = new Rate("ranking_badge_image_url_present_rate");
const rankingBadgeImageUrlCount = new Trend("ranking_badge_image_url_count");
const rankingBadgeResultCount = new Trend("ranking_badge_result_count");
const rankingBadgeDistinctImageUrlCount = new Trend("ranking_badge_distinct_image_url_count");

export const options = {
    stages: [
        { duration: "20s", target: 50 },
        { duration: "40s", target: 50 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:ranking_total_badge_image}": ["p(95)<1000", "p(99)<2500"],
        "http_req_duration{type:ranking_weekly_badge_image}": ["p(95)<1200", "p(99)<3000"],
        ranking_badge_response_valid_rate: ["rate>0.99"],
        ranking_badge_image_url_present_rate: ["rate>0.90"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
    return { token: login() };
}

function requestRankingList(token, path, type, apiName) {
    const params = authCookies(token);
    params.tags = {
        type,
        api: apiName,
    };

    const res = http.get(
        `${BASE_URL}${path}?page=${PAGE}&size=${SIZE}`,
        params
    );

    let rankings = [];
    if (res.status === 200) {
        rankings = res.json("data.rankings") || [];
    }

    const valid = res.status === 200 && Array.isArray(rankings);
    rankingBadgeResponseValidRate.add(valid, { api: apiName });

    if (valid) {
        const imageUrls = rankings
            .map((ranking) => ranking.badgeImageUrl)
            .filter((url) => typeof url === "string" && url.length > 0);

        rankingBadgeResultCount.add(rankings.length, { api: apiName });
        rankingBadgeImageUrlCount.add(imageUrls.length, { api: apiName });
        rankingBadgeDistinctImageUrlCount.add(new Set(imageUrls).size, { api: apiName });
        rankingBadgeImageUrlPresentRate.add(
            rankings.length > 0 && imageUrls.length / rankings.length >= 0.9,
            { api: apiName }
        );
    }

    check(res, {
        [`${apiName} status is 200`]: (r) => r.status === 200,
        [`${apiName} has rankings array`]: () => Array.isArray(rankings),
        [`${apiName} has badge image urls`]: () =>
            rankings.some((ranking) => ranking.badgeImageUrl),
    });
}

export default function (data) {
    requestRankingList(
        data.token,
        "/api/v1/rankings/points",
        "ranking_total_badge_image",
        TOTAL_RANKING_API
    );

    requestRankingList(
        data.token,
        "/api/v1/rankings/points/weekly",
        "ranking_weekly_badge_image",
        WEEKLY_RANKING_API
    );

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("ranking-badge-image-url-baseline");
