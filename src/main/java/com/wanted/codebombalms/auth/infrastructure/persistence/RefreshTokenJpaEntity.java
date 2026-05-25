package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RefreshTokenJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long refreshTokenId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, length = 512)
    private String token;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ===== 도메인 → 엔티티 (저장용) =====

    public static RefreshTokenJpaEntity from(RefreshToken refreshToken) {
        RefreshTokenJpaEntity e = new RefreshTokenJpaEntity();
        e.refreshTokenId = refreshToken.getRefreshTokenId();
        e.userId         = refreshToken.getUserId();
        e.token          = refreshToken.getToken();
        e.expiredAt      = refreshToken.getExpiredAt();
        return e;
    }

    // ===== 엔티티 → 도메인 (조회용) =====

    public RefreshToken toDomain() {
        return RefreshToken.restore(
                refreshTokenId,
                userId,
                token,
                expiredAt,
                createdAt
        );
    }
}