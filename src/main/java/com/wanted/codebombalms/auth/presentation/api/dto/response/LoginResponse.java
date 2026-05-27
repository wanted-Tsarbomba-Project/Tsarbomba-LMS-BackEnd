package com.wanted.codebombalms.auth.presentation.api.dto.response;

import com.wanted.codebombalms.auth.application.dto.LoginResult;

public record LoginResponse(String nickname) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.nickname());
    }
}