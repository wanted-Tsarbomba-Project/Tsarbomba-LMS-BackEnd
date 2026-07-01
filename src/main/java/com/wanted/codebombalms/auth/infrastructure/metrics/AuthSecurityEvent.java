package com.wanted.codebombalms.auth.infrastructure.metrics;

import lombok.Getter;

/** Auth/User 도메인 비정상 행위. category = 대시보드 드롭다운 단위, type = 세부 지표. */
@Getter
public enum AuthSecurityEvent {

    // 🅰 authn_attack — 인증 무차별 공격
    LOGIN_FAIL("authn_attack", "login_fail"),
    STEPUP_OTP_FAIL("authn_attack", "stepup_otp_fail"),
    STEPUP_LOCKED("authn_attack", "stepup_locked"),
    EMAIL_SEND_BLOCKED("authn_attack", "email_send_blocked"),
    PASSWORD_RESET_BLOCKED("authn_attack", "password_reset_blocked"),

    // 🅱 takeover — 계정 탈취/이상 접속
    SUSPICIOUS_LOGIN("takeover", "suspicious_login"),
    COUNTRY_CHANGED("takeover", "country_changed"),
    STEPUP_ISSUED("takeover", "stepup_issued"),

    // 🅲 oauth — 소셜 로그인 이상
    OAUTH_STATE_INVALID("oauth", "oauth_state_invalid"),
    OAUTH_TOKEN_EXCHANGE_FAIL("oauth", "oauth_token_exchange_fail"),
    OAUTH_USERINFO_FAIL("oauth", "oauth_userinfo_fail"),
    OAUTH_EMAIL_CONFLICT("oauth", "oauth_email_conflict"),
    OAUTH_EMAIL_NOT_VERIFIED("oauth", "oauth_email_not_verified"),

    // 🅳 token — 토큰/세션 남용
    REFRESH_TOKEN_INVALID("token", "refresh_token_invalid"),
    TEMP_TOKEN_INVALID("token", "temp_token_invalid"),
    LOCK_TOKEN_INVALID("token", "lock_token_invalid"),

    // 🅴 signup — 가입/인증코드 이상
    EMAIL_CODE_EXPIRED("signup", "email_code_expired");

    private final String category;
    private final String type;

    AuthSecurityEvent(String category, String type) {
        this.category = category;
        this.type = type;
    }
}
