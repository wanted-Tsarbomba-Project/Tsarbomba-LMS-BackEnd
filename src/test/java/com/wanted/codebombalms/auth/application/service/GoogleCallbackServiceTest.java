package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.dto.GoogleCallbackResult;
import com.wanted.codebombalms.auth.application.dto.OAuthUserInfo;
import com.wanted.codebombalms.auth.domain.model.GeoLocation;
import com.wanted.codebombalms.auth.domain.model.LoginHistory;
import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.model.TrustedDevice;
import com.wanted.codebombalms.auth.domain.repository.LoginHistoryRepository;
import com.wanted.codebombalms.auth.domain.repository.OAuthStateRepository;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.auth.domain.repository.TrustedDeviceRepository;
import com.wanted.codebombalms.auth.domain.service.GeoIpResolver;
import com.wanted.codebombalms.auth.infrastructure.oauth.GoogleOAuthClient;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleCallbackService 단위 테스트")
class GoogleCallbackServiceTest {

    private static final String CODE = "AUTH_CODE";
    private static final String STATE = "STATE";
    private static final String DEVICE_FP = "DEVICE_FP";
    private static final String EMAIL = "google@example.com";

    @Mock private OAuthStateRepository oAuthStateRepository;
    @Mock private GoogleOAuthClient googleOAuthClient;
    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private GeoIpResolver geoIpResolver;
    @Mock private LoginHistoryRepository loginHistoryRepository;
    @Mock private TrustedDeviceRepository trustedDeviceRepository;
    @Mock private HttpServletRequest request;

    @InjectMocks
    private GoogleCallbackService service;

    @BeforeEach
    void setUp() {
        given(oAuthStateRepository.validateAndDelete(STATE)).willReturn(true);
        given(googleOAuthClient.exchangeCodeForAccessToken(CODE)).willReturn("GOOGLE_AT");
        given(googleOAuthClient.fetchUserInfo("GOOGLE_AT"))
                .willReturn(new OAuthUserInfo(EMAIL, "구글유저", true));
    }

    @Test
    @DisplayName("기존 구글 회원 + 신규 기기 → 로그인 이력 기록 + 신뢰기기 신규 등록 후 토큰 발급")
    void 기존회원_신규기기_이력_및_신뢰기기등록() {
        // given
        User user = googleUser(1L);
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
        given(geoIpResolver.resolve(any())).willReturn(GeoLocation.unknown());
        given(trustedDeviceRepository.findByUserIdAndDeviceFp(1L, DEVICE_FP)).willReturn(Optional.empty());
        given(jwtTokenProvider.generateAccessToken(1L, "길동이", UserRole.STUDENT)).willReturn("AT");
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("RT");
        given(jwtTokenProvider.getRefreshExpiration()).willReturn(1209600000L);

        // when
        GoogleCallbackResult result = service.handleCallback(CODE, STATE, request, DEVICE_FP);

        // then
        assertFalse(result.newUser());
        assertEquals("AT", result.accessToken());
        assertEquals("RT", result.refreshToken());
        verify(loginHistoryRepository).save(any(LoginHistory.class));   // 이력 기록
        verify(trustedDeviceRepository).save(any(TrustedDevice.class)); // 신뢰기기 등록
        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("기존 구글 회원 + 기존 신뢰기기 → 신규 등록 없이 markUsed 갱신만 한다")
    void 기존회원_기존기기_markUsed_갱신() {
        // given
        User user = googleUser(1L);
        TrustedDevice existing = spy(TrustedDevice.register(1L, DEVICE_FP, "Chrome · macOS", null, null));
        given(userRepository.findByEmail(EMAIL)).willReturn(Optional.of(user));
        given(geoIpResolver.resolve(any())).willReturn(GeoLocation.unknown());
        given(trustedDeviceRepository.findByUserIdAndDeviceFp(1L, DEVICE_FP)).willReturn(Optional.of(existing));
        given(jwtTokenProvider.generateAccessToken(1L, "길동이", UserRole.STUDENT)).willReturn("AT");
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("RT");
        given(jwtTokenProvider.getRefreshExpiration()).willReturn(1209600000L);

        // when
        service.handleCallback(CODE, STATE, request, DEVICE_FP);

        // then
        verify(existing).markUsed(any(), any());                       // 기존 기기 갱신
        verify(trustedDeviceRepository).save(existing);
        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    // ===== 테스트 헬퍼 =====

    private User googleUser(Long userId) {
        return User.restore(
                userId,
                UserRole.STUDENT,
                EMAIL,
                "ENCODED_PW",
                "홍길동",
                "길동이",
                "010-1234-5678",
                AuthProvider.GOOGLE,
                null,
                true,
                false,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }
}
