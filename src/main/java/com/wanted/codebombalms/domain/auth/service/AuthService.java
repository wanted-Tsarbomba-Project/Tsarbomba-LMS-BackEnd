package com.wanted.codebombalms.domain.auth.service;

import com.wanted.codebombalms.domain.auth.dto.TokenPair;
import com.wanted.codebombalms.domain.auth.dto.request.LoginRequest;
import com.wanted.codebombalms.domain.auth.dto.request.SignupRequest;
import com.wanted.codebombalms.domain.auth.entity.RefreshToken;
import com.wanted.codebombalms.domain.auth.repository.RefreshTokenRepository;
import com.wanted.codebombalms.domain.user.entity.User;
import com.wanted.codebombalms.domain.user.exception.AuthErrorCode;
import com.wanted.codebombalms.domain.user.exception.UserErrorCode;
import com.wanted.codebombalms.domain.user.repository.UserRepository;
import com.wanted.codebombalms.global.error.exception.ConflictException;
import com.wanted.codebombalms.global.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(UserErrorCode.USER_EMAIL_DUPLICATED);
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new ConflictException(UserErrorCode.USER_NICKNAME_DUPLICATED);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.createLocalUser(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                request.getNickname(),
                request.getPhone()
        );

        userRepository.save(user);
    }

    @Transactional
    public TokenPair login(LoginRequest request) {
        // 1. 이메일로 회원 조회 (없으면 AUTH_LOGIN_FAIL)
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_LOGIN_FAIL));

        // 2. 비밀번호 검증 (틀리면 AUTH_LOGIN_FAIL)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_LOGIN_FAIL);
        }

        // 3. 계정 잠금 여부 확인
        if (user.isLocked()) {
            throw new ForbiddenException(UserErrorCode.USER_ACCOUNT_LOCKED);
        }

        // 4. JWT 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // 5. Refresh Token DB 저장
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusNanos(jwtTokenProvider.getRefreshExpiration() * 1_000_000L);

        refreshTokenRepository.save(
                RefreshToken.create(user.getUserId(), refreshToken, expiresAt)
        );

        return new TokenPair(accessToken, refreshToken);
    }
}