package com.wanted.codebombalms.user.domain.model;

public enum TermsType {

    SERVICE("v1.0"),   // 이용약관 (필수)
    PRIVACY("v1.0");   // 개인정보 수집·이용 (필수)

    private final String currentVersion;

    TermsType(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String currentVersion() {
        return currentVersion;
    }
}
