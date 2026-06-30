package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.dto.GoogleCallbackResult;
import com.wanted.codebombalms.auth.application.dto.OAuthUserInfo;
import com.wanted.codebombalms.auth.application.usecase.GoogleCallbackUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.GeoLocation;
import com.wanted.codebombalms.auth.domain.model.LoginHistory;
import com.wanted.codebombalms.auth.domain.model.OAuthTempData;
import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.model.TrustedDevice;
import com.wanted.codebombalms.auth.domain.repository.LoginHistoryRepository;
import com.wanted.codebombalms.auth.domain.repository.OAuthStateRepository;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.auth.domain.repository.TempTokenRepository;
import com.wanted.codebombalms.auth.domain.repository.TrustedDeviceRepository;
import com.wanted.codebombalms.auth.domain.service.GeoIpResolver;
import com.wanted.codebombalms.auth.infrastructure.oauth.GoogleOAuthClient;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    private final GeoIpResolver geoIpResolver;
    private final LoginHistoryRepository loginHistoryRepository;
    private final TrustedDeviceRepository trustedDeviceRepository;

    @Override
    public GoogleCallbackResult handleCallback(String code, String state, HttpServletRequest request, String deviceFp) {

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
            return issueTokens(user, request, deviceFp);
        }

        // 4. 신규 회원 → TEMP_TOKEN 발급 후 Redis 임시 저장
        String tempToken = UUID.randomUUID().toString();
        tempTokenRepository.save(tempToken, new OAuthTempData(userInfo.email(), userInfo.name()));
        return GoogleCallbackResult.newUser(tempToken);
    }

    /**
     * 기존 회원 토큰 발급 — 로그인 이력 기록 + 신뢰기기 upsert 후 AT/RT 발급.
     * step-up(이메일 OTP)은 제외한다 (구글이 이미 인증을 보장).
     */
    private GoogleCallbackResult issueTokens(User user, HttpServletRequest request, String deviceFp) {
        String ip = extractIpAddress(request);
        GeoLocation geo = geoIpResolver.resolve(ip);

        // 로그인 이력 기록 — 구글 인증 통과이므로 suspicious=false
        loginHistoryRepository.save(LoginHistory.record(
                user.getUserId(), ip, request.getHeader("User-Agent"),
                deviceFp, geo.country(), geo.city(), false));

        // 신뢰기기 upsert — 있으면 갱신, 없으면 등록 (유니크 (user_id, device_fp) 위반 방지)
        TrustedDevice device = trustedDeviceRepository
                .findByUserIdAndDeviceFp(user.getUserId(), deviceFp)
                .map(existing -> {
                    existing.markUsed(geo.country(), geo.city());
                    return existing;
                })
                .orElseGet(() -> TrustedDevice.register(
                        user.getUserId(),
                        deviceFp,
                        parseDeviceName(request.getHeader("User-Agent")),
                        geo.country(),
                        geo.city()
                ));
        trustedDeviceRepository.save(device);

        // 단일 세션 강제 후 AT/RT 발급
        refreshTokenRepository.deleteByUserId(user.getUserId());
        String token = jwtTokenProvider.generateAccessToken(
                user.getUserId(), user.getNickname(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
        refreshTokenRepository.save(
                RefreshToken.issue(user.getUserId(), refreshToken, jwtTokenProvider.getRefreshExpiration())
        );

        return GoogleCallbackResult.existingUser(token, refreshToken);
    }

    /** User-Agent → "브라우저 · OS" 표시명 (간이 파싱, StepUpVerifyService 와 동일 규칙) */
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
