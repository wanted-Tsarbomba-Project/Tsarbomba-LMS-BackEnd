package com.wanted.codebombalms.auth.presentation.api.dto.response;

import com.wanted.codebombalms.auth.application.dto.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record SessionStatusResponse(
        @Schema(description = "세션 만료까지 남은 초", example = "1180")
        long remainingSeconds,

        @Schema(description = "세션(Access Token) 만료 시각", example = "2026-07-23T15:00:00")
        LocalDateTime expiresAt,

        @Schema(description = "연장 없이 유지 가능한 한계 시각(Refresh Token 만료)", example = "2026-07-30T14:00:00")
        LocalDateTime sessionExpiresAt,

        @Schema(description = "지금 세션 연장이 가능한지", example = "true")
        boolean extendable
) {

    public static SessionStatusResponse from(SessionStatus status) {
        return new SessionStatusResponse(
                status.remainingSeconds(),
                status.expiresAt(),
                status.sessionExpiresAt(),
                status.extendable()
        );
    }
}
