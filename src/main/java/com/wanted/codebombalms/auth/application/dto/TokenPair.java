package com.wanted.codebombalms.auth.application.dto;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}