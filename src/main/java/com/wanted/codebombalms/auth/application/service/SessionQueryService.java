package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.dto.SessionStatus;
import com.wanted.codebombalms.auth.application.usecase.SessionQueryUseCase;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionQueryService implements SessionQueryUseCase {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public SessionStatus query(String accessToken, String refreshToken) {

        // 1. AT 만료 시각 → 잔여 초 (프론트 자동 로그아웃 모달용)
        LocalDateTime expiresAt = expirationOf(accessToken);
        long remainingSeconds = secondsUntil(expiresAt);

        // 2. RT 만료 시각 = 연장 없이 버틸 수 있는 한계
        LocalDateTime sessionExpiresAt = expirationOf(refreshToken);

        // 3. 연장 가능 여부 — /reissue 성공 조건과 동일하게 판정
        boolean extendable = sessionExpiresAt != null && isReissuable(refreshToken);

        return new SessionStatus(remainingSeconds, expiresAt, sessionExpiresAt, extendable);
    }

    /** 토큰 exp claim → LocalDateTime. 만료·위변조 토큰은 null (= 잔여 0 취급) */
    private LocalDateTime expirationOf(String token) {
        if (token == null) {
            return null;
        }
        try {
            Date expiration = jwtTokenProvider.getClaims(token).getExpiration();
            return LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private long secondsUntil(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            return 0L;
        }
        return Math.max(Duration.between(LocalDateTime.now(), expiresAt).toSeconds(), 0L);
    }

    /** RT 가 DB 에 등록돼 있고 아직 만료되지 않았는지 (RTR 이라 폐기된 RT 는 조회 안 됨) */
    private boolean isReissuable(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .filter(stored -> !stored.isExpired())
                .isPresent();
    }
}
