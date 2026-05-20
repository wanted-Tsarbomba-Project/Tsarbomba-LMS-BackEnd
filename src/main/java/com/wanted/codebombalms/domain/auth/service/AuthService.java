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
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.time.temporal.ChronoUnit;

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
        // 1. 이메일로 회원 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_LOGIN_FAIL));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_LOGIN_FAIL);
        }

        // 3. 계정 잠금 확인
        if (user.isLocked()) {
            throw new ForbiddenException(UserErrorCode.USER_ACCOUNT_LOCKED);
        }

        // 4. 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // 5. 기존 Refresh Token 전체 삭제 (단일 세션 정책)  ← ★ 추가됨
        refreshTokenRepository.deleteByUserId(user.getUserId());

        // 6. 새 Refresh Token DB 저장
        LocalDateTime expiresAt = LocalDateTime.now().plus(
                jwtTokenProvider.getRefreshExpiration(), ChronoUnit.MILLIS
        );
        refreshTokenRepository.save(RefreshToken.create(user.getUserId(), refreshToken, expiresAt));

        return new TokenPair(accessToken, refreshToken);
    }


    //로그아웃 - 해당 유저의 Refresh Token 전체 삭제
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * 토큰 재발급 (RTR: Refresh Token Rotation)
     * - 기존 Refresh Token 검증 후 새 Access + Refresh Token 발급
     * - 기존 Refresh Token DB에서 삭제, 새 토큰 DB 저장
     */
    @Transactional
    public TokenPair reissue(String refreshTokenValue) {
        // 1. JWT 자체 유효성 검증 (만료/위변조 → 예외 throw)
        jwtTokenProvider.validateRefreshToken(refreshTokenValue);

        // 2. DB에 저장된 토큰인지 확인 (탈취/임의 생성 방지)
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID));

        // 3. 유저 조회 (탈퇴/잠금 여부 확인)
        User user = userRepository.findByUserIdAndDeletedAtIsNull(stored.getUserId())
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID));

        if (user.isLocked()) {
            throw new ForbiddenException(UserErrorCode.USER_ACCOUNT_LOCKED);
        }

        // 4. 새 토큰 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // 5. 기존 Refresh Token 삭제 (RTR: 1회용)
        refreshTokenRepository.delete(stored);

        // 6. 새 Refresh Token 저장
        LocalDateTime expiresAt = LocalDateTime.now().plus(
                jwtTokenProvider.getRefreshExpiration(), ChronoUnit.MILLIS
        );
        refreshTokenRepository.save(RefreshToken.create(user.getUserId(), newRefreshToken, expiresAt));

        return new TokenPair(newAccessToken, newRefreshToken);
    }
}