package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.dto.GoogleCallbackResult;
import com.wanted.codebombalms.auth.application.dto.OAuthUserInfo;
import com.wanted.codebombalms.auth.application.usecase.GoogleCallbackUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.OAuthTempData;
import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.repository.OAuthStateRepository;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.auth.domain.repository.TempTokenRepository;
import com.wanted.codebombalms.auth.infrastructure.oauth.GoogleOAuthClient;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GoogleCallbackService implements GoogleCallbackUseCase {

    private final OAuthStateRepository oAuthStateRepository;
    private final GoogleOAuthClient googleOAuthClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TempTokenRepository tempTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public GoogleCallbackResult handleCallback(String code, String state) {

        // 1. state 검증 (CSRF 방지, 단일 사용)
        if (!oAuthStateRepository.validateAndDelete(state)) {
            throw new ValidationException(AuthErrorCode.OAUTH_STATE_INVALID);
        }

        // 2. code → access token → 구글 사용자 정보
        String accessToken = googleOAuthClient.exchangeCodeForAccessToken(code);
        OAuthUserInfo userInfo = googleOAuthClient.fetchUserInfo(accessToken);

        // 2-1. 구글에서 인증되지 않은 이메일은 거부 (신규 가입 신뢰성 확보)
        if (!userInfo.emailVerified()) {
            throw new ValidationException(AuthErrorCode.OAUTH_EMAIL_NOT_VERIFIED);
        }

        // 3. 이메일로 기존 회원 조회
        Optional<User> existing = userRepository.findByEmail(userInfo.email());
        if (existing.isPresent()) {
            User user = existing.get();

            // 3-1. 동일 이메일 LOCAL 계정 → 충돌(오류 정책)
            if (user.getProvider() != AuthProvider.GOOGLE) {
                throw new ValidationException(AuthErrorCode.OAUTH_EMAIL_ALREADY_LOCAL);
            }

            // 3-2. 기존 구글 회원 → 토큰 발급 (로그인)
            return issueTokens(user);
        }

        // 4. 신규 회원 → TEMP_TOKEN 발급 후 Redis 임시 저장
        String tempToken = UUID.randomUUID().toString();
        tempTokenRepository.save(tempToken, new OAuthTempData(userInfo.email(), userInfo.name()));
        return GoogleCallbackResult.newUser(tempToken);
    }

    /** 기존 회원 토큰 발급 — 단일 세션 강제 후 AT/RT 생성·저장 (LoginService 동일 패턴) */
    private GoogleCallbackResult issueTokens(User user) {
        refreshTokenRepository.deleteByUserId(user.getUserId());

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(), user.getNickname(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        refreshTokenRepository.save(
                RefreshToken.issue(user.getUserId(), refreshToken, jwtTokenProvider.getRefreshExpiration())
        );

        return GoogleCallbackResult.existingUser(accessToken, refreshToken);
    }
}
