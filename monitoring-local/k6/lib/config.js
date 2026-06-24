// k6 시나리오 공통 설정 (LMS Auth/User 도메인)

// 테스트 대상 앱 주소. 도커 k6 컨테이너 → 호스트 앱(8080)
export const BASE_URL = __ENV.BASE_URL || "http://host.docker.internal:8080";

// 사용자가 요청 사이에 쉬는 시간 (실제 사용자 흉내)
export const MIN_SLEEP = Number(__ENV.MIN_SLEEP || 0.5);
export const MAX_SLEEP = Number(__ENV.MAX_SLEEP || 2);

export function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function randomSleep() {
    return Math.random() * (MAX_SLEEP - MIN_SLEEP) + MIN_SLEEP;
}

// 매번 다른 이메일 생성 (중복확인 테스트용 — 실제 가입 X)
export function randomEmail() {
    return `loadtest_${randomInt(1, 1000000)}@example.com`;}
