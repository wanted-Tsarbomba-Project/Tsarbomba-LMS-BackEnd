// k6 공통 인증 헬퍼 (JWT 쿠키 기반)
//
// 우리 인증은 accessToken "쿠키"로 동작한다(JwtAuthenticationFilter 가 쿠키를 읽음).
// 로그인은 토큰을 응답 body 가 아니라 Set-Cookie(accessToken/refreshToken) 로 준다.
//
// public 엔드포인트(/api/v1/auth/**, /courses/**, /lectures/**)는 이 헬퍼가 필요 없다.
// 인증 필요 엔드포인트(/chat/**, /users/** 등)·admin(/admin/**)에서만 쓴다.
//
// ── 사용법 ────────────────────────────────────────────────
//   import { login, authCookies } from "../../lib/auth.js";
//
//   export function setup() {                       // 부하 시작 전 1회만 로그인
//     return { token: login() };                    // 로그인 부하가 본 시나리오에 안 섞임
//   }
//   export default function (data) {
//     const params = authCookies(data.token);       // accessToken 쿠키
//     params.tags = { api: "GET /chat/list" };      // 태그는 이렇게 덧붙인다
//     http.get(`${BASE_URL}/api/v1/chat/list`, params);
//   }
//   ⚠️ k6 babel 은 객체 스프레드를 안 받는다. { ...authCookies(t), tags } 처럼 쓰지 말 것.
//
//   admin 시나리오: LOGIN_EMAIL/LOGIN_PASSWORD 를 admin 계정으로 주면 된다.
//     docker compose run --rm -e LOGIN_EMAIL=admin@x.com -e LOGIN_PASSWORD=... k6 run ...
// ──────────────────────────────────────────────────────────

import http from "k6/http";
import { BASE_URL } from "./config.js";

// loadtest DB 에 시드된 테스트 계정. 실제 계정으로 환경변수 덮어쓰기 권장.
const LOGIN_EMAIL = __ENV.LOGIN_EMAIL || "u01@test.com";
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || "Test1234!";
const DEVICE_ID = __ENV.DEVICE_ID || "learning-loadtest-device";

// 로그인해서 accessToken 쿠키 값을 반환한다. (setup() 에서 호출 권장)
export function login(email = LOGIN_EMAIL, password = LOGIN_PASSWORD) {
    http.cookieJar().set(BASE_URL, "deviceId", DEVICE_ID);

    const res = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({ email, password }),
        {
            headers: {
                "Content-Type": "application/json",
                Cookie: `deviceId=${DEVICE_ID}`,
            },
            tags: { api: "POST /auth/login" },
        }
    );

    if (res.status !== 200) {
        throw new Error(
            `[auth] 로그인 실패 status=${res.status} — loadtest DB 에 계정(${email})이 시드돼 있는지 확인. body=${res.body}`
        );
    }

    // k6 babel 은 옵셔널 체이닝(?.)을 안 받아서 풀어서 꺼낸다.
    const jar = res.cookies || {};
    const cookie = jar.accessToken && jar.accessToken[0];
    const accessToken = cookie ? cookie.value : extractCookieValue(res.headers["Set-Cookie"], "accessToken");
    if (!accessToken) {
        throw new Error(`[auth] 응답에 accessToken 쿠키가 없음 — 로그인 응답/쿠키 설정 확인. body=${res.body}`);
    }
    return accessToken;
}

function extractCookieValue(setCookieHeader, name) {
    if (!setCookieHeader) {
        return undefined;
    }

    const marker = `${name}=`;
    const start = setCookieHeader.indexOf(marker);
    if (start < 0) {
        return undefined;
    }

    const valueStart = start + marker.length;
    const valueEnd = setCookieHeader.indexOf(";", valueStart);
    return valueEnd < 0
        ? setCookieHeader.substring(valueStart)
        : setCookieHeader.substring(valueStart, valueEnd);
}

// 요청 옵션 객체. const p = authCookies(token); p.tags = {...}; http.get(url, p) 형태로 사용.
export function authCookies(accessToken) {
    return {
        headers: {
            Cookie: `accessToken=${accessToken}; deviceId=${DEVICE_ID}`,
        },
    };
}
