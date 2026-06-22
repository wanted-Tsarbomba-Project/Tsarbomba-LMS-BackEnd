package com.wanted.codebombalms.auth.domain.model;

/** 신규 소셜 회원의 임시 보관 정보 (TEMP_TOKEN 으로 조회) */
public record OAuthTempData(
        String email,
        String name
) {
}
