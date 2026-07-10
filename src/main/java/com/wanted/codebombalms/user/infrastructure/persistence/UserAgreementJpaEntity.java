package com.wanted.codebombalms.user.infrastructure.persistence;

import com.wanted.codebombalms.user.domain.model.TermsType;
import com.wanted.codebombalms.user.domain.model.UserAgreement;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_agreement",
        indexes = {
                @Index(name = "idx_user_agreement_user_type_time",
                        columnList = "user_id, terms_type, agreed_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAgreementJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_agreement_id")
    private Long userAgreementId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "terms_type", nullable = false, length = 20)
    private TermsType termsType;

    @Column(name = "terms_version", nullable = false, length = 20)
    private String termsVersion;

    @Column(name = "agreed", nullable = false)
    private boolean agreed;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    public static UserAgreementJpaEntity from(UserAgreement agreement) {
        UserAgreementJpaEntity e = new UserAgreementJpaEntity();
        e.userAgreementId = agreement.getUserAgreementId();
        e.userId          = agreement.getUserId();
        e.termsType       = agreement.getTermsType();
        e.termsVersion    = agreement.getTermsVersion();
        e.agreed          = agreement.isAgreed();
        e.agreedAt        = agreement.getAgreedAt();
        return e;
    }

    public UserAgreement toDomain() {
        return UserAgreement.restore(
                userAgreementId, userId, termsType,
                termsVersion, agreed, agreedAt
        );
    }
}
