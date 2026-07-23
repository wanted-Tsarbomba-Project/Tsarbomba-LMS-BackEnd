package com.wanted.codebombalms.auth.application.dto;

import java.time.LocalDateTime;

/**
 * 현재 세션의 잔여 시간 정보.
 *
 * <p>AT/RT 모두 HttpOnly 쿠키라 프론트가 exp 를 읽을 수 없어 서버가 계산해 내려준다.
 *
 * @param remainingSeconds  Access Token 만료까지 남은 초 (만료됐으면 0)
 * @param expiresAt         Access Token 만료 시각
 * @param sessionExpiresAt  Refresh Token 만료 시각 (연장 없이 유지 가능한 한계)
 * @param extendable        지금 세션 연장이 가능한지 (RT 가 DB 에 살아있는지)
 */
public record SessionStatus(
        long remainingSeconds,
        LocalDateTime expiresAt,
        LocalDateTime sessionExpiresAt,
        boolean extendable
) {
}
