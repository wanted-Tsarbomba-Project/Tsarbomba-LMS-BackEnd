package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.command.LoginCommand;
import com.wanted.codebombalms.auth.application.dto.LoginResult;
import com.wanted.codebombalms.auth.application.usecase.LoginUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.LoginHistory;
import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.repository.LoginHistoryRepository;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResult login(LoginCommand command, HttpServletRequest request) {

        // 1. 이메일로 회원 조회
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_LOGIN_FAIL));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(command.rawpassword(), user.getPassword())) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_LOGIN_FAIL);
        }

        // 3. 계정 잠금 확인
        if (user.isLocked()) {
            throw new ForbiddenException(UserErrorCode.USER_ACCOUNT_LOCKED);
        }

        // 4. 단일 세션 강제
        refreshTokenRepository.deleteByUserId(user.getUserId());

        // 5. 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getNickname(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // 6. RT 저장
        refreshTokenRepository.save(
                RefreshToken.issue(user.getUserId(), refreshToken, jwtTokenProvider.getRefreshExpiration())
        );

        // 7. 로그인 이력 기록
        loginHistoryRepository.save(
                LoginHistory.record(
                        user.getUserId(),
                        extractIpAddress(request),
                        request.getHeader("User-Agent"),
                        null,
                        null,
                        null,
                        false
                )
        );

        return new LoginResult(accessToken, refreshToken, user.getNickname());
    }

    /** 프록시 환경(X-Forwarded-For) 고려한 클라이언트 IP 추출 */
    private String extractIpAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();   // 첫 번째 IP가 원본 클라이언트
        }
        return request.getRemoteAddr();
    }
}