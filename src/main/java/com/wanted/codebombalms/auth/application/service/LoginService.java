package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.command.LoginCommand;
import com.wanted.codebombalms.auth.application.dto.LoginResult;
import com.wanted.codebombalms.auth.application.usecase.LoginUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.GeoLocation;
import com.wanted.codebombalms.auth.domain.model.LoginHistory;
import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.model.StepUpChallenge;
import com.wanted.codebombalms.auth.domain.model.TrustedDevice;
import com.wanted.codebombalms.auth.domain.repository.LoginHistoryRepository;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.auth.domain.repository.StepUpTokenRepository;
import com.wanted.codebombalms.auth.domain.repository.TrustedDeviceRepository;
import com.wanted.codebombalms.auth.domain.service.EmailSender;
import com.wanted.codebombalms.auth.domain.service.GeoIpResolver;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wanted.codebombalms.auth.domain.repository.LockTokenRepository;
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginService implements LoginUseCase {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final TrustedDeviceRepository trustedDeviceRepository;
    private final StepUpTokenRepository stepUpTokenRepository;
    private final GeoIpResolver geoIpResolver;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResult login(LoginCommand command, HttpServletRequest request, String deviceFp) {

        // 1~3. 인증 (이메일 / 비밀번호 / 계정 잠금)
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_LOGIN_FAIL));
        if (!passwordEncoder.matches(command.rawpassword(), user.getPassword())) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_LOGIN_FAIL);
        }
        if (user.isLocked()) {
            throw new ForbiddenException(UserErrorCode.USER_ACCOUNT_LOCKED);
        }

        // 4. 기기 지문 + 지역(GeoIP)
        String ip = extractIpAddress(request);
        GeoLocation geo = geoIpResolver.resolve(ip);

        // 5. 적응형 판정 — 신뢰 기기 1차 + 위치 보조
        Optional<TrustedDevice> trusted =
                trustedDeviceRepository.findByUserIdAndDeviceFp(user.getUserId(), deviceFp);
        boolean suspicious = trusted.isEmpty() || isCountryChanged(trusted.get(), geo);
        boolean stepUpRequired = user.getRole() == UserRole.STUDENT && suspicious;

        // 6. 로그인 이력 기록 (의심 여부 = 신뢰기기/국가 기준, 역할 무관)
        loginHistoryRepository.save(LoginHistory.record(
                user.getUserId(), ip, request.getHeader("User-Agent"),
                deviceFp, geo.country(), geo.city(), suspicious));

        // 7. 미신뢰/위치 급변 → step-up
        if (stepUpRequired) {
            return issueStepUp(user, deviceFp, geo);
        }

        // 8. 신뢰 기기 → 정식 로그인 (마지막 사용 갱신)
        trusted.ifPresent(device -> {
            device.markUsed(geo.country(), geo.city());
            trustedDeviceRepository.save(device);
        });
        return issueTokens(user);
    }

    private LoginResult issueStepUp(User user, String deviceFp, GeoLocation geo) {
        String stepUpToken = UUID.randomUUID().toString();

        // 멱등성 유지: 같은 기기에 이미 유효한 챌린지가 있으면 재사용 (코드 재발급/메일 재발송 차단)
        Optional<String> existing =
                stepUpTokenRepository.reserveDeviceChallenge(user.getUserId(), deviceFp, stepUpToken);
        if (existing.isPresent()) {
            return LoginResult.stepUp(existing.get(), maskEmail(user.getEmail()));
        }

        // 신규 발급 — OTP(step-up) 생성
        String code = generateCode();
        stepUpTokenRepository.save(stepUpToken,
                new StepUpChallenge(user.getUserId(), deviceFp, geo.country(), code));

        // 계정 잠금 토큰 발급 + 링크 조립
        String lockToken = UUID.randomUUID().toString();
        lockTokenRepository.save(lockToken, user.getUserId());
        String lockUrl = lockUrlBase + "?token=" + lockToken;

        emailSender.sendStepUpCode(user.getEmail(), code, lockUrl);
        return LoginResult.stepUp(stepUpToken, maskEmail(user.getEmail()));
    }

    private LoginResult issueTokens(User user) {
        refreshTokenRepository.deleteByUserId(user.getUserId());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getNickname(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
        refreshTokenRepository.save(
                RefreshToken.issue(user.getUserId(), refreshToken, jwtTokenProvider.getRefreshExpiration()));
        return LoginResult.success(accessToken, refreshToken, user.getNickname(), user.getRole());
    }

    private boolean isCountryChanged(TrustedDevice device, GeoLocation geo) {
        return device.getLastCountry() != null
                && geo.country() != null
                && !device.getLastCountry().equals(geo.country());
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    /** 프록시 환경(X-Forwarded-For) 고려한 클라이언트 IP 추출 */
    private String extractIpAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private final LockTokenRepository lockTokenRepository;

    @Value("${app.lock-url:http://localhost:8080/api/v1/auth/lock}")
    private String lockUrlBase;
}
