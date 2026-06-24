// User 도메인 — GET /api/v1/users (학생 목록, Admin) 단일 baseline
//
// 목적: users(role, created_at) 인덱스 부재로 인한 풀스캔 + filesort 비용을 baseline 으로 박는다.
//   → 인덱스(@Table(indexes=...)) 반영 후 같은 스크립트로 전후 비교.
//
// ⚠️ 전제 (5단계 시드):
//   - loadtest DB(3307)에 학생 다수(5만+) 시드 필요(18명으론 filesort 가 안 드러남).
//     → src/main/resources/db/seed/01_users_loadtest_students.sql
//   - 이 API 는 hasRole('ADMIN') → admin 계정으로 로그인해야 한다(아래 실행 예시 참고).
//
// 실행 (monitoring/ 에서):
//   docker compose run --rm \
//     -e RESULT_NAME=user-student-list-before \
//     -e LOGIN_EMAIL=admin@test.com -e LOGIN_PASSWORD=Test1234! \
//     k6 run -o experimental-prometheus-rw /scripts/user/01-student-list-baseline.js

import http from "k6/http";
import { check, sleep } from "k6";
import { BASE_URL, randomSleep } from "../../lib/config.js";
import { login, authCookies } from "../../lib/auth.js";
import { createSummaryHandler } from "../../lib/summary.js";

export const options = {
    // 0 → 10명까지 올렸다 유지 후 종료.
    // ⚠️ 1M 행 + 단건 505ms 쿼리라 VU 50 이면 Hikari 풀(기본 10) 고갈 → 연결 자체가 막혀
    //    "느림"이 아니라 "붕괴(dial timeout)"가 된다. p95 를 깨끗이 재려면 요청이 성공하는
    //    선에서 부하를 준다. (after-index 비교도 같은 VU 로 맞춤)
    stages: [
        { duration: "15s", target: 10 },
        { duration: "45s", target: 10 },
        { duration: "10s", target: 0 },
    ],
    thresholds: {
        http_req_failed: ["rate<0.01"],                          // 실패율 1% 미만
        "http_req_duration{type:student_list}": ["p(95)<500"],   // 조회형 합격 기준 (CONVENTION §6)
    },
    summaryTrendStats: ["avg", "min", "med", "p(90)", "p(95)", "p(99)", "max"],
};

// 부하 시작 전 1회만 로그인(admin) → 로그인 부하가 본 측정에 안 섞인다.
export function setup() {
    return { token: login() };
}

export default function (data) {
    // k6 babel 이 객체 스프레드(...)를 안 받아서, 헬퍼 반환 객체에 tags 만 붙여 쓴다.
    const params = authCookies(data.token);
    params.tags = { type: "student_list", api: "GET /users" };   // threshold·Grafana 분리용 태그

    // 관리자 화면 첫 페이지(가입 최신순 20명) — 인덱스 없으면 매 요청 풀스캔 + filesort
    const res = http.get(`${BASE_URL}/api/v1/users?page=0&size=20`, params);

    check(res, {
        "status is 200": (r) => r.status === 200,
        "has content array": (r) => Array.isArray(r.json("data.content")),
    });

    sleep(randomSleep());
}

export const handleSummary = createSummaryHandler("user-student-list-baseline");
