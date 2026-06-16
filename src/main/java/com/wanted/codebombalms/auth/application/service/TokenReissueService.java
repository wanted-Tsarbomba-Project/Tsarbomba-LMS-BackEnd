package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.dto.TokenPair;
import com.wanted.codebombalms.auth.application.usecase.TokenReissueUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenReissueService implements TokenReissueUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public TokenPair reissue(String refreshToken) {

        // 1. JWT 자체 유효성 검증 (만료 / 위변조)
        jwtTokenProvider.validateRefreshToken(refreshToken);

        // 2. DB 에 등록된 토큰인지 확인
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID));

        // 3. DB 만료 확인 (이중 안전망)
        if (stored.isExpired()) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
        }

        // 4. 유저 조회 + 상태 확인
        Long userId = stored.getUserId();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        if (user.isLocked()) {
            throw new ForbiddenException(UserErrorCode.USER_ACCOUNT_LOCKED);
        }

        // 5. 새 토큰 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getNickname(),user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // 6. RTR — 기존 RT 삭제 + 새 RT 저장 (1회용)
        refreshTokenRepository.deleteByUserId(user.getUserId());
        refreshTokenRepository.save(
                RefreshToken.issue(user.getUserId(), newRefreshToken, jwtTokenProvider.getRefreshExpiration())
        );

        return new TokenPair(newAccessToken, newRefreshToken);
    }
}
