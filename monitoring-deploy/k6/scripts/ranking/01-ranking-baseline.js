import http from "k6/http";
import { check, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";
import { BASE_URL, randomSleep } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

const PAGE = __ENV.RANKING_PAGE || "0";
const SIZE = __ENV.RANKING_SIZE || "20";

const TOTAL_RANKING_API = "GET /rankings/points";
const WEEKLY_RANKING_API = "GET /rankings/points/weekly";
const MY_RANKING_API = "GET /rankings/points/me";

const rankingResponseValidRate = new Rate("ranking_response_valid_rate");
const rankingResultCount = new Trend("ranking_result_count");
const rankingMyRank = new Trend("ranking_my_rank");
const rankingTotalPoint = new Trend("ranking_total_point");

export const options = {
    stages: [
        { duration: "20s", target: 50 },
        { duration: "40s", target: 50 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:ranking_total}": ["p(95)<800", "p(99)<2000"],
        "http_req_duration{type:ranking_weekly}": ["p(95)<1000", "p(99)<2500"],
        "http_req_duration{type:ranking_me}": ["p(95)<800", "p(99)<2000"],
        ranking_response_valid_rate: ["rate>0.99"],
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export function setup() {
    return { token: login() };
}

function requestRankingList(token, path, type, apiName) {
    const params = authCookies(token);
    params.tags = {
        type: type,
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
    rankingResponseValidRate.add(valid, { api: apiName });

    if (valid) {
        rankingResultCount.add(rankings.length, { api: apiName });
    }

    check(res, {
        [`${apiName} status is 200`]: (r) => r.status === 200,
        [`${apiName} has rankings array`]: () => Array.isArray(rankings),
    });
}

function requestMyRanking(token) {
    const params = authCookies(token);
    params.tags = {
        type: "ranking_me",
        api: MY_RANKING_API,
    };

    const res = http.get(`${BASE_URL}/api/v1/rankings/points/me`, params);

    let data = {};
    if (res.status === 200) {
        data = res.json("data") || {};
    }

    const valid = res.status === 200 && data.rank !== undefined;
    rankingResponseValidRate.add(valid, { api: MY_RANKING_API });

    if (valid) {
        rankingMyRank.add(Number(data.rank), { api: MY_RANKING_API });
        rankingTotalPoint.add(Number(data.totalPoint || 0), { api: MY_RANKING_API });
    }

    check(res, {
        "my ranking status is 200": (r) => r.status === 200,
        "my ranking has rank": () => data.rank !== undefined,
    });
}

export default function (data) {
    requestRankingList(
        data.token,
        "/api/v1/rankings/points",
        "ranking_total",
        TOTAL_RANKING_API
    );

    requestRankingList(
        data.token,
        "/api/v1/rankings/points/weekly",
        "ranking_weekly",
        WEEKLY_RANKING_API
    );

    requestMyRanking(data.token);

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("ranking-baseline");
