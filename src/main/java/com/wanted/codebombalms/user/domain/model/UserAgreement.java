package com.wanted.codebombalms.user.domain.model;

import java.time.LocalDateTime;

public class UserAgreement {

    private Long userAgreementId;
    private Long userId;
    private TermsType termsType;
    private String termsVersion;
    private boolean agreed;
    private LocalDateTime agreedAt;

    private UserAgreement() {}

    // ===== Getter =====
    public Long getUserAgreementId()   { return userAgreementId; }
    public Long getUserId()            { return userId; }
    public TermsType getTermsType()    { return termsType; }
    public String getTermsVersion()    { return termsVersion; }
    public boolean isAgreed()          { return agreed; }
    public LocalDateTime getAgreedAt() { return agreedAt; }

    // ===== 신규 동의 생성 — 현재 약관 버전을 서버가 스탬프 =====
    public static UserAgreement agree(Long userId, TermsType termsType) {
        UserAgreement a = new UserAgreement();
        a.userId       = userId;
        a.termsType    = termsType;
        a.termsVersion = termsType.currentVersion();
        a.agreed       = true;
        a.agreedAt     = LocalDateTime.now();
        return a;
    }

    // ===== 영속성 복원 — Adapter 전용 =====
    public static UserAgreement restore(
            Long userAgreementId, Long userId, TermsType termsType,
            String termsVersion, boolean agreed, LocalDateTime agreedAt
    ) {
        UserAgreement a = new UserAgreement();
        a.userAgreementId = userAgreementId;
        a.userId          = userId;
        a.termsType       = termsType;
        a.termsVersion    = termsVersion;
        a.agreed          = agreed;
        a.agreedAt        = agreedAt;
        return a;
    }
}
