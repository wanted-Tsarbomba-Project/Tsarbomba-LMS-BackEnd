package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.command.LoginCommand;
import com.wanted.codebombalms.auth.application.dto.TokenPair;
import com.wanted.codebombalms.auth.application.usecase.LoginUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginService implements LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public TokenPair login(LoginCommand command) {

        // 1. 이메일로 회원 조회 — 이메일 없음 / 비번 불일치 모두 동일 에러 (보안)
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_LOGIN_FAIL));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(command.rawPassword(), user.getPassword())) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_LOGIN_FAIL);
        }

        // 3. 계정 잠금 확인
        if (user.isLocked()) {
            throw new ForbiddenException(UserErrorCode.USER_ACCOUNT_LOCKED);
        }

        // 4. 단일 세션 강제 — 기존 RT 전부 삭제
        refreshTokenRepository.deleteByUserId(user.getUserId());

        // 5. 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // 6. RT 저장
        refreshTokenRepository.save(
                RefreshToken.issue(user.getUserId(), refreshToken, jwtTokenProvider.getRefreshExpiration())
        );

        return new TokenPair(accessToken, refreshToken);
    }
}