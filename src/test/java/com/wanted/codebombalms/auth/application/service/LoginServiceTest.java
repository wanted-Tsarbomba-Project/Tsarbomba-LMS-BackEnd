package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.command.LoginCommand;
import com.wanted.codebombalms.auth.application.dto.TokenPair;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.LoginHistory;
import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.repository.LoginHistoryRepository;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginService 단위 테스트")
class LoginServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private LoginHistoryRepository loginHistoryRepository;
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
    @DisplayName("정상 로그인 시 TokenPair를 반환하고 새 Refresh Token + LoginHistory를 저장한다.")
    void 로그인_성공() {
        // given
        User user = createUser(1L, "test@example.com", "ENCODED_PW", false);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Test1234!", "ENCODED_PW")).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(1L, "길동이", UserRole.STUDENT)).willReturn("ACCESS_TOKEN");        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("REFRESH_TOKEN");
        given(jwtTokenProvider.getRefreshExpiration()).willReturn(1209600000L);

        // when
        TokenPair pair = loginService.login(command, httpRequest);

        // then
        assertEquals("ACCESS_TOKEN", pair.accessToken());
        assertEquals("REFRESH_TOKEN", pair.refreshToken());
        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    @Test
    @DisplayName("이메일이 없으면 UnauthorizedException(AUTH_LOGIN_FAIL)을 던진다.")
    void 이메일_없음_예외() {
        // given
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.empty());

        // when & then
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> loginService.login(command, httpRequest)
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
                () -> loginService.login(command, httpRequest)
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
                () -> loginService.login(command, httpRequest)
        );
        assertEquals(UserErrorCode.USER_ACCOUNT_LOCKED, ex.getErrorCode());
        verify(refreshTokenRepository, never()).save(any());
        verify(loginHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인 시 기존 Refresh Token을 전부 삭제하고 새 토큰을 저장한다 (단일 세션 강제).")
    void 단일_세션_강제() {
        // given
        User user = createUser(1L, "test@example.com", "ENCODED_PW", false);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Test1234!", "ENCODED_PW")).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(1L, "길동이", UserRole.STUDENT)).willReturn("ACCESS_TOKEN");        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("REFRESH_TOKEN");
        given(jwtTokenProvider.getRefreshExpiration()).willReturn(1209600000L);

        // when
        loginService.login(command, httpRequest);

        // then — 삭제 후 저장 순서 검증
        var inOrder = inOrder(refreshTokenRepository);
        inOrder.verify(refreshTokenRepository).deleteByUserId(1L);
        inOrder.verify(refreshTokenRepository).save(any(RefreshToken.class));
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