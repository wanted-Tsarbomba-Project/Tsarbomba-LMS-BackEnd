package com.wanted.codebombalms.auth.application.dto;

import com.wanted.codebombalms.user.domain.model.UserRole;

public record LoginResult(
        boolean stepUpRequired,
        String accessToken,
        String refreshToken,
        String nickname,
        UserRole role,
        String stepUpToken,
        String maskedEmail
) {
    public static LoginResult success(String accessToken, String refreshToken, String nickname, UserRole role) {
        return new LoginResult(false, accessToken, refreshToken, nickname, role, null, null);
    }

    public static LoginResult stepUp(String stepUpToken, String maskedEmail) {
        return new LoginResult(true, null, null, null, null, stepUpToken, maskedEmail);
    }
}
