package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.command.LoginCommand;
import com.wanted.codebombalms.auth.application.dto.LoginResult;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.GeoLocation;
import com.wanted.codebombalms.auth.domain.model.LoginHistory;
import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.model.TrustedDevice;
import com.wanted.codebombalms.auth.domain.repository.LockTokenRepository;
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
import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginService 단위 테스트")
class LoginServiceTest {

    private static final String DEVICE_FP = "DEVICE_FP";

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private LoginHistoryRepository loginHistoryRepository;
    @Mock private TrustedDeviceRepository trustedDeviceRepository;
    @Mock private StepUpTokenRepository stepUpTokenRepository;
    @Mock private LockTokenRepository lockTokenRepository;
    @Mock private GeoIpResolver geoIpResolver;
    @Mock private EmailSender emailSender;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private HttpServletRequest httpRequest;

    @InjectMocks
    private LoginService loginService;

    private LoginCommand command;

    @BeforeEach
    void setUp() {
        command = new LoginCommand("test@example.com", "Test1234!");
    }

    @Test
    @DisplayName("신뢰 기기 로그인 시 LoginResult(토큰 2개 + 닉네임)를 반환하고 새 Refresh Token + LoginHistory를 저장한다.")
    void 신뢰기기_로그인_성공() {
        // given
        User user = createUser(1L, "test@example.com", "ENCODED_PW", false);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Test1234!", "ENCODED_PW")).willReturn(true);
        given(geoIpResolver.resolve(any())).willReturn(GeoLocation.unknown());
        given(trustedDeviceRepository.findByUserIdAndDeviceFp(1L, DEVICE_FP))
                .willReturn(Optional.of(TrustedDevice.register(1L, DEVICE_FP, "Chrome", null, null)));
        given(jwtTokenProvider.generateAccessToken(1L, "길동이", UserRole.STUDENT)).willReturn("ACCESS_TOKEN");
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("REFRESH_TOKEN");
        given(jwtTokenProvider.getRefreshExpiration()).willReturn(1209600000L);

        // when
        LoginResult result = loginService.login(command, httpRequest, DEVICE_FP);

        // then
        assertFalse(result.stepUpRequired());
        assertEquals("ACCESS_TOKEN", result.accessToken());
        assertEquals("REFRESH_TOKEN", result.refreshToken());
        assertEquals("길동이", result.nickname());
        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    @Test
    @DisplayName("미신뢰 기기 로그인 시 step-up(이메일 OTP)을 요구하고 정식 토큰은 발급하지 않는다.")
    void 미신뢰기기_stepup() {
        // given
        User user = createUser(1L, "test@example.com", "ENCODED_PW", false);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Test1234!", "ENCODED_PW")).willReturn(true);
        given(geoIpResolver.resolve(any())).willReturn(GeoLocation.unknown());
        given(trustedDeviceRepository.findByUserIdAndDeviceFp(1L, DEVICE_FP)).willReturn(Optional.empty());

        // when
        LoginResult result = loginService.login(command, httpRequest, DEVICE_FP);

        // then
        assertTrue(result.stepUpRequired());
        assertNotNull(result.stepUpToken());
        assertNotNull(result.maskedEmail());
        assertNull(result.accessToken());
        verify(emailSender).sendStepUpCode(eq("test@example.com"), anyString(), anyString());        verify(stepUpTokenRepository).save(anyString(), any());
        verify(loginHistoryRepository).save(any(LoginHistory.class));
        verify(jwtTokenProvider, never()).generateAccessToken(anyLong(), anyString(), any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("이메일이 없으면 UnauthorizedException(AUTH_LOGIN_FAIL)을 던진다.")
    void 이메일_없음_예외() {
        // given
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.empty());

        // when & then
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> loginService.login(command, httpRequest, DEVICE_FP)
        );
        assertEquals(AuthErrorCode.AUTH_LOGIN_FAIL, ex.getErrorCode());
        verify(refreshTokenRepository, never()).save(any());
        verify(loginHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("비밀번호가 불일치하면 UnauthorizedException(AUTH_LOGIN_FAIL)을 던진다.")
    void 비밀번호_불일치_예외() {
        // given
        User user = createUser(1L, "test@example.com", "ENCODED_PW", false);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Test1234!", "ENCODED_PW")).willReturn(false);

        // when & then
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> loginService.login(command, httpRequest, DEVICE_FP)
        );
        assertEquals(AuthErrorCode.AUTH_LOGIN_FAIL, ex.getErrorCode());
        verify(refreshTokenRepository, never()).save(any());
        verify(loginHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("잠긴 계정이면 ForbiddenException(USER_ACCOUNT_LOCKED)을 던진다.")
    void 계정_잠금_예외() {
        // given
        User user = createUser(1L, "test@example.com", "ENCODED_PW", true);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Test1234!", "ENCODED_PW")).willReturn(true);

        // when & then
        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> loginService.login(command, httpRequest, DEVICE_FP)
        );
        assertEquals(UserErrorCode.USER_ACCOUNT_LOCKED, ex.getErrorCode());
        verify(refreshTokenRepository, never()).save(any());
        verify(loginHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("신뢰 기기 로그인 시 기존 Refresh Token을 전부 삭제하고 새 토큰을 저장한다 (단일 세션 강제).")
    void 단일_세션_강제() {
        // given
        User user = createUser(1L, "test@example.com", "ENCODED_PW", false);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Test1234!", "ENCODED_PW")).willReturn(true);
        given(geoIpResolver.resolve(any())).willReturn(GeoLocation.unknown());
        given(trustedDeviceRepository.findByUserIdAndDeviceFp(1L, DEVICE_FP))
                .willReturn(Optional.of(TrustedDevice.register(1L, DEVICE_FP, "Chrome", null, null)));
        given(jwtTokenProvider.generateAccessToken(1L, "길동이", UserRole.STUDENT)).willReturn("ACCESS_TOKEN");
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("REFRESH_TOKEN");
        given(jwtTokenProvider.getRefreshExpiration()).willReturn(1209600000L);

        // when
        loginService.login(command, httpRequest, DEVICE_FP);

        // then — 삭제 후 저장 순서 검증
        var inOrder = inOrder(refreshTokenRepository);
        inOrder.verify(refreshTokenRepository).deleteByUserId(1L);
        inOrder.verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("미신뢰 기기에서 동일 요청이 중복 유입돼도 step-up 메일은 1회만 발송한다. (멱등)")
    void 미신뢰기기_중복요청_메일_1회만() {
        // given — 같은 userId+deviceFp 로 두 번째 요청은 이미 발급된 토큰을 재사용
        User user = createUser(1L, "test@example.com", "ENCODED_PW", false);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Test1234!", "ENCODED_PW")).willReturn(true);
        given(geoIpResolver.resolve(any())).willReturn(GeoLocation.unknown());
        given(trustedDeviceRepository.findByUserIdAndDeviceFp(1L, DEVICE_FP)).willReturn(Optional.empty());
        given(stepUpTokenRepository.reserveDeviceChallenge(eq(1L), eq(DEVICE_FP), anyString()))
                .willReturn(Optional.empty())               // 1번째 요청 → 발급권 선점
                .willReturn(Optional.of("EXISTING_TOKEN"));  // 2번째 요청 → 기존 토큰 재사용

        // when — 동일 로그인 2회(재시도 시뮬레이션)
        LoginResult first = loginService.login(command, httpRequest, DEVICE_FP);
        LoginResult second = loginService.login(command, httpRequest, DEVICE_FP);

        // then — 메일·잠금토큰은 신규 발급분 1회만, 2번째는 같은 토큰 재사용
        assertTrue(first.stepUpRequired());
        assertTrue(second.stepUpRequired());
        assertEquals("EXISTING_TOKEN", second.stepUpToken());
        verify(emailSender, times(1)).sendStepUpCode(eq("test@example.com"), anyString(), anyString());
        verify(lockTokenRepository, times(1)).save(anyString(), anyLong());  // 잠금 토큰도 1회만 (#10)
        verify(stepUpTokenRepository, times(2)).save(anyString(), any());    // 챌린지는 예약 전 저장 → 호출 2회(2번째 미사용)
    }

    // ===== 테스트 헬퍼 =====

    private User createUser(Long userId, String email, String encodedPassword, boolean isLocked) {
        return User.restore(
                userId,
                UserRole.STUDENT,
                email,
                encodedPassword,
                "홍길동",
                "길동이",
                "010-1234-5678",
                AuthProvider.LOCAL,
                null,
                false,
                isLocked,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }
}
