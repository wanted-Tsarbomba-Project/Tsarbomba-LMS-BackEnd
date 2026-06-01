package com.wanted.codebombalms.auth.domain.model;

import java.time.LocalDateTime;

public class RefreshToken {

    private Long refreshTokenId;
    private Long userId;
    private String token;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;

    private RefreshToken() {}

    // ===== Getter =====
    public Long getRefreshTokenId()      { return refreshTokenId; }
    public Long getUserId()              { return userId; }
    public String getToken()             { return token; }
    public LocalDateTime getExpiredAt()  { return expiredAt; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    // ===== 정적 팩토리 — 신규 발급 =====

    public static RefreshToken issue(Long userId, String token, long ttlMillis) {
        RefreshToken rt = new RefreshToken();
        rt.userId    = userId;
        rt.token     = token;
        rt.expiredAt = LocalDateTime.now().plusNanos(ttlMillis * 1_000_000);
        return rt;
    }

    // ===== 영속성 복원 — Adapter 전용 =====

    public static RefreshToken restore(
            Long refreshTokenId,
            Long userId,
            String token,
            LocalDateTime expiredAt,
            LocalDateTime createdAt
    ) {
        RefreshToken rt = new RefreshToken();
        rt.refreshTokenId = refreshTokenId;
        rt.userId         = userId;
        rt.token          = token;
        rt.expiredAt      = expiredAt;
        rt.createdAt      = createdAt;
        return rt;
    }

    // ===== 도메인 행위 =====

    // 만료 여부 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }
}