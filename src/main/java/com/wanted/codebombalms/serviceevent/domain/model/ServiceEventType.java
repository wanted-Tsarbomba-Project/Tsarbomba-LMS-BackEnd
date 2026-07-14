package com.wanted.codebombalms.serviceevent.domain.model;

import static com.wanted.codebombalms.serviceevent.domain.model.ServiceEventCategory.*;

/**
 * 서비스 이벤트 세부 타입 — 각 타입은 카테고리에 소속.
 * DB(service_event.event_type)에는 code(소문자)로 저장.
 */
public enum ServiceEventType {

    // authn_attack
    LOGIN_FAIL(AUTHN_ATTACK, "login_fail"),
    STEPUP_OTP_FAIL(AUTHN_ATTACK, "stepup_otp_fail"),
    STEPUP_LOCKED(AUTHN_ATTACK, "stepup_locked"),
    EMAIL_SEND_BLOCKED(AUTHN_ATTACK, "email_send_blocked"),
    PASSWORD_RESET_BLOCKED(AUTHN_ATTACK, "password_reset_blocked"),

    // takeover
    SUSPICIOUS_LOGIN(TAKEOVER, "suspicious_login"),
    COUNTRY_CHANGED(TAKEOVER, "country_changed"),
    STEPUP_ISSUED(TAKEOVER, "stepup_issued"),

    // oauth
    OAUTH_STATE_INVALID(OAUTH, "oauth_state_invalid"),
    OAUTH_TOKEN_EXCHANGE_FAIL(OAUTH, "oauth_token_exchange_fail"),
    OAUTH_USERINFO_FAIL(OAUTH, "oauth_userinfo_fail"),
    OAUTH_EMAIL_CONFLICT(OAUTH, "oauth_email_conflict"),
    OAUTH_EMAIL_NOT_VERIFIED(OAUTH, "oauth_email_not_verified"),

    // token
    REFRESH_TOKEN_INVALID(TOKEN, "refresh_token_invalid"),
    TEMP_TOKEN_INVALID(TOKEN, "temp_token_invalid"),
    LOCK_TOKEN_INVALID(TOKEN, "lock_token_invalid"),

    // signup
    EMAIL_CODE_EXPIRED(SIGNUP, "email_code_expired"),

    // enrollment
    ENROLL_CREATED(ENROLLMENT, "enroll_created"),
    ENROLL_CANCELLED(ENROLLMENT, "enroll_cancelled"),

    // learning
    LESSON_COMPLETED(LEARNING, "lesson_completed"),
    PROBLEM_SOLVED(LEARNING, "problem_solved"),
    PROBLEM_SET_COMPLETED(LEARNING, "problem_set_completed"),
    EXPLANATION_VIEWED(LEARNING, "explanation_viewed"),

    // content
    COURSE_PUBLISHED(CONTENT, "course_published"),
    COURSE_CREATED(CONTENT, "course_created"),
    COURSE_DELETED(CONTENT, "course_deleted"),
    LECTURE_DELETED(CONTENT, "lecture_deleted"),

    // reward
    POINT_GRANTED(REWARD, "point_granted"),
    BADGE_ACQUIRED(REWARD, "badge_acquired"),

    // chatbot
    ROOM_CREATED(CHATBOT, "room_created"),
    AI_ANSWERED(CHATBOT, "ai_answered"),

    // admin_audit
    PERMISSION_TOGGLED(ADMIN_AUDIT, "permission_toggled"),
    ACCOUNT_LOCKED(ADMIN_AUDIT, "account_locked"),
    ACCOUNT_UNLOCKED(ADMIN_AUDIT, "account_unlocked"),
    ALERT_RESOLVED(ADMIN_AUDIT, "alert_resolved"),

    // http_anomaly
    HTTP_5XX(HTTP_ANOMALY, "http_5xx"),
    HTTP_502_EXTERNAL(HTTP_ANOMALY, "http_502_external"),
    AUTH_401_SPIKE(HTTP_ANOMALY, "auth_401_spike"),
    ACCESS_403(HTTP_ANOMALY, "access_403"),
    SLOW_REQUEST(HTTP_ANOMALY, "slow_request"),

    // ops_metric
    CONCURRENT_SAMPLE(OPS_METRIC, "concurrent_sample");

    private final ServiceEventCategory category;
    private final String code;

    ServiceEventType(ServiceEventCategory category, String code) {
        this.category = category;
        this.code = code;
    }

    public ServiceEventCategory category() {
        return category;
    }

    public String code() {
        return code;
    }
}
