package com.wanted.codebombalms.auth.application.dto;

/**
 * 구글 콜백 처리 결과.
 * - 기존 회원: accessToken/refreshToken 발급 (newUser=false)
 * - 신규 회원: tempToken 발급 (newUser=true)
 */
public record GoogleCallbackResult(
        boolean newUser,
        String accessToken,
        String refreshToken,
        String tempToken
) {
    public static GoogleCallbackResult existingUser(String accessToken, String refreshToken) {
        return new GoogleCallbackResult(false, accessToken, refreshToken, null);
    }

    public static GoogleCallbackResult newUser(String tempToken) {
        return new GoogleCallbackResult(true, null, null, tempToken);
    }
}
