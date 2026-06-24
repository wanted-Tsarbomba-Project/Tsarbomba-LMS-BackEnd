package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.command.StepUpVerifyCommand;
import com.wanted.codebombalms.auth.application.dto.LoginResult;
import com.wanted.codebombalms.auth.application.usecase.StepUpVerifyUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.GeoLocation;
import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.model.StepUpChallenge;
import com.wanted.codebombalms.auth.domain.model.TrustedDevice;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.auth.domain.repository.StepUpTokenRepository;
import com.wanted.codebombalms.auth.domain.repository.TrustedDeviceRepository;
import com.wanted.codebombalms.auth.domain.service.GeoIpResolver;
import com.wanted.codebombalms.global.domain.common.error.exception.TooManyRequestsException;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StepUpVerifyService implements StepUpVerifyUseCase {

    private static final int MAX_ATTEMPTS = 5;

    private final StepUpTokenRepository stepUpTokenRepository;
    private final UserRepository userRepository;
    private final TrustedDeviceRepository trustedDeviceRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GeoIpResolver geoIpResolver;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResult verify(StepUpVerifyCommand command, HttpServletRequest request) {

        // 1. 챌린지 토큰 검증
        String token = command.stepUpToken();
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_STEP_UP_TOKEN_INVALID);
        }
        StepUpChallenge challenge = stepUpTokenRepository.find(token)
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_STEP_UP_TOKEN_INVALID));

        // 2. OTP 검증 (실패 시 시도 카운트 → 초과면 429)
        if (!challenge.code().equals(command.code())) {
            int attempts = stepUpTokenRepository.incrementAttempts(token);
            if (attempts >= MAX_ATTEMPTS) {
                stepUpTokenRepository.delete(token);
                throw new TooManyRequestsException(AuthErrorCode.AUTH_STEP_UP_TOO_MANY);
            }
            throw new ValidationException(AuthErrorCode.AUTH_CODE_INVALID);
        }

        // 3. 성공 — 토큰 단일 사용 소비
        stepUpTokenRepository.delete(token);

        User user = userRepository.findByUserId(challenge.userId())
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_LOGIN_FAIL));

        // 4. 신뢰 기기 등록 (옵션)
        if (command.trustDevice()) {
            GeoLocation geo = geoIpResolver.resolve(extractIpAddress(request));
            trustedDeviceRepository.save(TrustedDevice.register(
                    user.getUserId(),
                    challenge.deviceFp(),
                    parseDeviceName(request.getHeader("User-Agent")),
                    geo.country(),
                    geo.city()
            ));
        }

        // 5. 정식 토큰 발급 (단일 세션 강제)
        refreshTokenRepository.deleteByUserId(user.getUserId());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getNickname(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
        refreshTokenRepository.save(
                RefreshToken.issue(user.getUserId(), refreshToken, jwtTokenProvider.getRefreshExpiration()));
        return LoginResult.success(accessToken, refreshToken, user.getNickname(), user.getRole());
    }

    /** User-Agent → "브라우저 · OS" 표시명 (간이 파싱) */
    private String parseDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown device";
        }
        String browser = userAgent.contains("Edg") ? "Edge"
                : userAgent.contains("Chrome") ? "Chrome"
                  : userAgent.contains("Firefox") ? "Firefox"
                    : userAgent.contains("Safari") ? "Safari" : "Unknown";
        String os = userAgent.contains("Windows") ? "Windows"
                : userAgent.contains("Mac OS") ? "macOS"
                  : userAgent.contains("Android") ? "Android"
                    : (userAgent.contains("iPhone") || userAgent.contains("iPad")) ? "iOS"
                      : userAgent.contains("Linux") ? "Linux" : "Unknown";
        return browser + " · " + os;
    }

    private String extractIpAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
