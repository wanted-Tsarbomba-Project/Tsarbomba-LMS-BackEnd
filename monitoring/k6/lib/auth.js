import http from "k6/http";
import { BASE_URL } from "./config.js";

const LOGIN_EMAIL = __ENV.LOGIN_EMAIL || "u01@test.com";
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || "Test1234!";

export function login(email = LOGIN_EMAIL, password = LOGIN_PASSWORD) {
    const res = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({ email, password }),
        {
            headers: { "Content-Type": "application/json" },
            tags: { api: "POST /auth/login" },
        }
    );

    if (res.status !== 200) {
        throw new Error(
            `[auth] login failed status=${res.status}. Check loadtest seed account (${email}). body=${res.body}`
        );
    }

    const jar = res.cookies || {};
    const cookie = jar.accessToken && jar.accessToken[0];
    const accessToken = cookie ? cookie.value : undefined;
    if (!accessToken) {
        throw new Error("[auth] accessToken cookie was not found in login response.");
    }

    return accessToken;
}

export function authCookies(accessToken) {
    return { cookies: { accessToken } };
}
