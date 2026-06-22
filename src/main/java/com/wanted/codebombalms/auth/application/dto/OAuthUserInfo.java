package com.wanted.codebombalms.auth.application.dto;

/** 구글 userinfo 응답에서 우리가 쓰는 값만 추린 결과 */
public record OAuthUserInfo(
        String email,
        String name,
        boolean emailVerified
) {
}