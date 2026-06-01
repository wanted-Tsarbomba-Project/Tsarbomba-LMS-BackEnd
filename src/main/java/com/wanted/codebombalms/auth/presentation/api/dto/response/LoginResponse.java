package com.wanted.codebombalms.auth.presentation.api.dto.response;

import com.wanted.codebombalms.auth.application.dto.LoginResult;

public record LoginResponse(String nickname, String role) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.nickname(), result.role(). name());
    }
}
