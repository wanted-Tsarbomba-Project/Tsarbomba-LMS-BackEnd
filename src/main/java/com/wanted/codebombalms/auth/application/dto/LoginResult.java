package com.wanted.codebombalms.auth.application.dto;

public record LoginResult(
        String accessToken,
        String refreshToken,
        String nickname
) {
}