// k6 시나리오 공통 설정 — 배포(deploy) 대상
//
// ⚠️ monitoring-local 과 다른 점: BASE_URL 기본값이 "배포 BE EIP" 다.
//    그래서 -e BASE_URL 안 줘도 배포 ② Spring(43.200.241.157:8080)으로 쏜다.
//    (로컬 앱에 쏘려면 monitoring-local 을 써라 — 여긴 배포 전용)

// 배포 ② Spring EIP. IP 바뀌면 여기만 수정(docs/deploy/OVERVIEW.md §3 참조).
export const BASE_URL = __ENV.BASE_URL || "http://43.200.241.157:8080";

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
    return `loadtest_${randomInt(1, 1000000)}@example.com`;
}
