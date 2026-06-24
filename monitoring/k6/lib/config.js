export const BASE_URL = __ENV.BASE_URL || "http://host.docker.internal:8080";

export const MIN_SLEEP = Number(__ENV.MIN_SLEEP || 0.5);
export const MAX_SLEEP = Number(__ENV.MAX_SLEEP || 2);

export function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function randomSleep() {
    return Math.random() * (MAX_SLEEP - MIN_SLEEP) + MIN_SLEEP;
}

export function randomEmail() {
    return `loadtest_${randomInt(1, 1000000)}@example.com`;
}
