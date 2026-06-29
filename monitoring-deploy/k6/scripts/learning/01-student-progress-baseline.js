// Learning 트랙 A — GET /api/v1/courses/{courseId}/users/learning-progress baseline
//
// 목적: 강좌별 학생 학습률 목록 조회의 수강생별 반복 조회 비용을 baseline 으로 박는다.
//   → 학생 수가 늘 때 HTTP p95, custom timer, Loki durationMs 가 같이 증가하는지 확인한다.
//
// 전제:
//   - 앱은 loadtest 프로파일로 실행한다.
//   - admin/operator 권한 계정으로 로그인해야 한다.
//   - COURSE_ID 에 해당하는 강좌에 수강생/강의/문제세트/진행률 데이터가 있어야 병목이 드러난다.
//
// 실행 (monitoring/ 에서):
//   docker compose run --rm -e LOGIN_EMAIL=admin@test.com -e LOGIN_PASSWORD=Test1234! \
//     -e COURSE_ID=2000 -e RESULT_NAME=learning-student-progress-before \
//     k6 run -o experimental-prometheus-rw /scripts/learning/01-student-progress-baseline.js

import http from "k6/http";
import { check, sleep } from "k6";
import { BASE_URL, randomSleep } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

const COURSE_ID = __ENV.COURSE_ID || "2000";     // 강좌 아이디가 2000번인 부분에서 테스트를 한다.

export const options = {
    stages: [
        { duration: "20s", target: 30 },    // VU를 0명에서 30명까지 증가
        { duration: "40s", target: 30 },    // VU 30명 유지
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],
        "http_req_duration{type:student-progress}": ["p(95)<800"],
        // 요청 실패율이 1% 미만이어야 함, student-progress 타입의 요청의 95%가 800ms 미만이어야 함
        // 이 타입의 요청만 위 threshold, 즉 성공기준을 적용할 수 있음
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

// 부하 시작 전 1회만 로그인한다. 로그인 부하는 본 측정에 섞지 않는다.
export function setup() {
    return { token: login() };
}

// 요청 실행
export default function (data) {
    const params = authCookies(data.token);
    params.tags = {
        type: "student-progress",
        api: "GET /courses/{courseId}/users/learning-progress",
    };

    const res = http.get(
        `${BASE_URL}/api/v1/courses/${COURSE_ID}/users/learning-progress?page=0`,
        params
    );

    // 응답 검증
    check(res, {
        "status is 200": (r) => r.status === 200,
        "has page content array": (r) => Array.isArray(r.json("data.content")),
    });

    // 사용자 대기 시간, 저 randomSleep 는 글로벌 config에 있음
    sleep(randomSleep());
}

// 결과 저장, 테스트 결과 자동 저장
export const handleSummary = createSummaryHandler("learning-student-progress-baseline");
