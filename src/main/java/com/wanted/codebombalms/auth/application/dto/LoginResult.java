package com.wanted.codebombalms.auth.application.dto;

import com.wanted.codebombalms.user.domain.model.UserRole;

public record LoginResult(
        String accessToken,
        String refreshToken,
        String nickname,
        UserRole role
) {
}
