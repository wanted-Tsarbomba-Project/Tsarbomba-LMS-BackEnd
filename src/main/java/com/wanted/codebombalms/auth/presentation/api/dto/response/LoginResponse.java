package com.wanted.codebombalms.auth.presentation.api.dto.response;

import com.wanted.codebombalms.auth.application.dto.LoginResult;

public record LoginResponse(
        boolean stepUpRequired,
        String nickname,
        String role,
        String maskedEmail
) {
    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(
                result.stepUpRequired(),
                result.nickname(),
                result.role() == null ? null : result.role().name(),
                result.maskedEmail()
        );
    }
}
