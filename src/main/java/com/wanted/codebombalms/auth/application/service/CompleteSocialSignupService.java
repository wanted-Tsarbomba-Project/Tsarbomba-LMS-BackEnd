package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.dto.TokenPair;
import com.wanted.codebombalms.auth.application.usecase.CompleteSocialSignupUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.OAuthTempData;
import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.auth.domain.repository.TempTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CompleteSocialSignupService implements CompleteSocialSignupUseCase {

    private final TempTokenRepository tempTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public TokenPair complete(String tempToken, String nickname, String phone) {

        // 1. TEMP_TOKEN 검증 + 소비 (단일 사용). 없으면 401
        OAuthTempData data = tempTokenRepository.findAndDelete(tempToken)
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_TEMP_TOKEN_INVALID));

        // 2. 닉네임 중복 검사 (USR-003)
        if (userRepository.existsByNickname(nickname)) {
            throw new ValidationException(UserErrorCode.USER_NICKNAME_DUPLICATED);
        }

        // 3. 이메일 중복 방어 (중복 제출 등 — USR-002)
        if (userRepository.existsByEmail(data.email())) {
            throw new ValidationException(UserErrorCode.USER_EMAIL_DUPLICATED);
        }

        // 4. 소셜 회원 생성 (GOOGLE, STUDENT)
        User saved = userRepository.save(
                User.createSocialUser(data.email(), data.name(), nickname, phone, AuthProvider.GOOGLE)
        );

        // 5. 토큰 발급 (가입 직후 바로 로그인)
        String accessToken = jwtTokenProvider.generateAccessToken(
                saved.getUserId(), saved.getNickname(), saved.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(saved.getUserId());

        refreshTokenRepository.save(
                RefreshToken.issue(saved.getUserId(), refreshToken, jwtTokenProvider.getRefreshExpiration())
        );

        return new TokenPair(accessToken, refreshToken);
    }
}
