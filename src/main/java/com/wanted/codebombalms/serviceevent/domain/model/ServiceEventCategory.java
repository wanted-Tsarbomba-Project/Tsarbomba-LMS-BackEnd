package com.wanted.codebombalms.serviceevent.domain.model;

/**
 * 서비스 이벤트 대분류.
 * DB(service_event.category)에는 code(소문자)로 저장된다.
 */
public enum ServiceEventCategory {

    // 보안 (기존 AuthSecurityEvent 카테고리 승계)
    AUTHN_ATTACK("authn_attack"),
    TAKEOVER("takeover"),
    OAUTH("oauth"),
    TOKEN("token"),
    SIGNUP("signup"),

    // 비즈니스
    ENROLLMENT("enrollment"),
    LEARNING("learning"),
    CONTENT("content"),
    REWARD("reward"),
    CHATBOT("chatbot"),
    ADMIN_AUDIT("admin_audit"),

    // 인프라 신호 / 운영 지표
    HTTP_ANOMALY("http_anomaly"),
    OPS_METRIC("ops_metric");

    private final String code;

    ServiceEventCategory(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    /** 보안 카테고리 여부 — summary 집계에서 "보안 이벤트" 묶음에 사용 */
    public boolean isSecurity() {
        return this == AUTHN_ATTACK || this == TAKEOVER || this == OAUTH
                || this == TOKEN || this == SIGNUP;
    }
}
