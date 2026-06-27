import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { BASE_URL } from '../../lib/config.js';
import { authCookies, login } from '../../lib/auth.js';
import { createSummaryHandler } from '../../lib/summary.js';

const PROBLEM_SET_ID = __ENV.PROBLEM_SET_ID || '830001';
const USER_ID_START = Number(__ENV.USER_ID_START || '830001');
const USER_COUNT = Number(__ENV.USER_COUNT || '300');
const PASSWORD = __ENV.PASSWORD || 'Test1234!';

const entryDuration = new Trend('problem_set_entry_duration', true);
const validRate = new Rate('problem_set_entry_valid_rate');

export const options = {
    scenarios: {
        problem_set_entry: {
            executor: 'shared-iterations',
            vus: Number(__ENV.VUS || '50'),
            iterations: Number(__ENV.ITERATIONS || '300'),
            maxDuration: __ENV.MAX_DURATION || '2m',
        },
    },
    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'problem_set_entry_valid_rate': ['rate>0.99'],
        'http_req_duration{type:problem_set_entry}': ['p(95)<800'],
    },
    summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

export function setup() {
    const tokens = [];

    for (let i = 0; i < USER_COUNT; i += 1) {
        const userId = USER_ID_START + i;
        const email = `problem-entry-loadtest-${userId}@test.com`;

        tokens.push(login(email, PASSWORD));
    }

    return { tokens };
}

export default function (data) {
    const token = data.tokens[__ITER % data.tokens.length];

    const params = authCookies(token);
    params.headers = { Accept: 'application/json' };
    params.tags = { type: 'problem_set_entry' };

    const res = http.get(`${BASE_URL}/api/v1/problem-sets/${PROBLEM_SET_ID}`, params);

    entryDuration.add(res.timings.duration, { type: 'problem_set_entry' });

    let body = null;
    try {
        body = res.json();
    } catch (e) {
        body = null;
    }

    const hasData = body !== null && body.data !== null && body.data !== undefined;
    const problems = hasData && Array.isArray(body.data.problems)
        ? body.data.problems
        : [];

    const valid = check(res, {
        'status is 200': (r) => r.status === 200,
        'has data': () => hasData,
        'problem count is 20': () => hasData && body.data.totalProblemCount === 20,
        'problems length is 20': () => problems.length === 20,
        'start code exists': () => problems.every((problem) => problem.startCode !== null),
    });

    validRate.add(valid);

    sleep(0.1);
}

export const handleSummary = createSummaryHandler('problem-set-entry-baseline');
