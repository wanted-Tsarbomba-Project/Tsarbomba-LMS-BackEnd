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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    // ===== 헬퍼 메서드 =====
    private SignupRequest createSignupRequest() {
        SignupRequest request = BeanUtils.instantiateClass(SignupRequest.class);
        ReflectionTestUtils.setField(request, "email", "test@example.com");
        ReflectionTestUtils.setField(request, "password", "Test1234!");
        ReflectionTestUtils.setField(request, "name", "홍길동");
        ReflectionTestUtils.setField(request, "nickname", "테스트닉");
        ReflectionTestUtils.setField(request, "phone", "010-1234-5678");
        return request;
    }

    private LoginRequest createLoginRequest() {
        LoginRequest request = BeanUtils.instantiateClass(LoginRequest.class);
        ReflectionTestUtils.setField(request, "email", "test@example.com");
        ReflectionTestUtils.setField(request, "password", "Test1234!");
        return request;
    }

    private User createTestUser(boolean locked) {
        User user = User.createLocalUser(
                "test@example.com", "encodedPassword", "홍길동", "테스트닉", "010-1234-5678"
        );
        ReflectionTestUtils.setField(user, "userId", 1L);
        ReflectionTestUtils.setField(user, "isLocked", locked);
        return user;
    }

    // ============================================================
    // 회원가입 (signup)
    // ============================================================
    @Nested
    @DisplayName("회원가입")
    class Signup {

        @Test
        @DisplayName("성공 시 User를 저장한다.")
        void 회원가입_성공() {
            // given
            SignupRequest request = createSignupRequest();
            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            given(userRepository.existsByNickname(request.getNickname())).willReturn(false);
            given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");

            // when
            authService.signup(request);

            // then
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("이메일이 중복되면 ConflictException(USER_EMAIL_DUPLICATED) 을 던진다.")
        void 이메일_중복_시_예외() {
            // given
            SignupRequest request = createSignupRequest();
            given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

            // when & then
            ConflictException ex = assertThrows(
                    ConflictException.class,
                    () -> authService.signup(request)
            );
            assertEquals(UserErrorCode.USER_EMAIL_DUPLICATED, ex.getErrorCode());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("닉네임이 중복되면 ConflictException(USER_NICKNAME_DUPLICATED) 을 던진다.")
        void 닉네임_중복_시_예외() {
            // given
            SignupRequest request = createSignupRequest();
            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            given(userRepository.existsByNickname(request.getNickname())).willReturn(true);

            // when & then
            ConflictException ex = assertThrows(
                    ConflictException.class,
                    () -> authService.signup(request)
            );
            assertEquals(UserErrorCode.USER_NICKNAME_DUPLICATED, ex.getErrorCode());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("비밀번호를 암호화하여 저장한다.")
        void 비밀번호_암호화() {
            // given
            SignupRequest request = createSignupRequest();
            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            given(userRepository.existsByNickname(request.getNickname())).willReturn(false);
            given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");

            // when
            authService.signup(request);

            // then
            verify(passwordEncoder).encode(request.getPassword());
        }
    }

    // ============================================================
    // 로그인 (login)
    // ============================================================
    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("성공 시 TokenPair 를 반환하고 RefreshToken 을 저장한다.")
        void 로그인_성공() {
            // given
            LoginRequest request = createLoginRequest();
            User user = createTestUser(false);

            given(userRepository.findByEmailAndDeletedAtIsNull(request.getEmail()))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
            given(jwtTokenProvider.generateAccessToken(user.getUserId(), user.getRole()))
                    .willReturn("access-token");
            given(jwtTokenProvider.generateRefreshToken(user.getUserId()))
                    .willReturn("refresh-token");
            given(jwtTokenProvider.getRefreshExpiration()).willReturn(1209600000L);

            // when
            TokenPair result = authService.login(request);

            // then
            assertEquals("access-token", result.accessToken());
            assertEquals("refresh-token", result.refreshToken());
            verify(refreshTokenRepository).deleteByUserId(user.getUserId()); // 단일 세션 강제
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("이메일이 존재하지 않으면 AUTH_LOGIN_FAIL 을 던진다.")
        void 이메일_없음_예외() {
            // given
            LoginRequest request = createLoginRequest();
            given(userRepository.findByEmailAndDeletedAtIsNull(request.getEmail()))
                    .willReturn(Optional.empty());

            // when & then
            UnauthorizedException ex = assertThrows(
                    UnauthorizedException.class,
                    () -> authService.login(request)
            );
            assertEquals(AuthErrorCode.AUTH_LOGIN_FAIL, ex.getErrorCode());
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 AUTH_LOGIN_FAIL 을 던진다.")
        void 비밀번호_불일치_예외() {
            // given
            LoginRequest request = createLoginRequest();
            User user = createTestUser(false);

            given(userRepository.findByEmailAndDeletedAtIsNull(request.getEmail()))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

            // when & then
            UnauthorizedException ex = assertThrows(
                    UnauthorizedException.class,
                    () -> authService.login(request)
            );
            assertEquals(AuthErrorCode.AUTH_LOGIN_FAIL, ex.getErrorCode());
            verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("계정이 잠금 상태면 USER_ACCOUNT_LOCKED 를 던진다.")
        void 계정_잠금_예외() {
            // given
            LoginRequest request = createLoginRequest();
            User lockedUser = createTestUser(true);

            given(userRepository.findByEmailAndDeletedAtIsNull(request.getEmail()))
                    .willReturn(Optional.of(lockedUser));
            given(passwordEncoder.matches(request.getPassword(), lockedUser.getPassword())).willReturn(true);

            // when & then
            ForbiddenException ex = assertThrows(
                    ForbiddenException.class,
                    () -> authService.login(request)
            );
            assertEquals(UserErrorCode.USER_ACCOUNT_LOCKED, ex.getErrorCode());
            verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        }
    }

    // ============================================================
    // 로그아웃 (logout)
    // ============================================================
    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("해당 유저의 RefreshToken 을 전부 삭제한다.")
        void 로그아웃_성공() {
            // given
            Long userId = 1L;

            // when
            authService.logout(userId);

            // then
            verify(refreshTokenRepository).deleteByUserId(userId);
        }
    }

    // ============================================================
    // 토큰 재발급 (reissue, RTR)
    // ============================================================
    @Nested
    @DisplayName("토큰 재발급")
    class Reissue {

        @Test
        @DisplayName("성공 시 새 TokenPair 반환, 기존 토큰 삭제 + 새 토큰 저장.")
        void 재발급_성공() {
            // given
            String oldRefreshToken = "old-refresh-token";
            User user = createTestUser(false);
            RefreshToken stored = RefreshToken.create(user.getUserId(), oldRefreshToken, null);

            given(refreshTokenRepository.findByToken(oldRefreshToken)).willReturn(Optional.of(stored));
            given(userRepository.findByUserIdAndDeletedAtIsNull(user.getUserId()))
                    .willReturn(Optional.of(user));
            given(jwtTokenProvider.generateAccessToken(user.getUserId(), user.getRole()))
                    .willReturn("new-access-token");
            given(jwtTokenProvider.generateRefreshToken(user.getUserId()))
                    .willReturn("new-refresh-token");
            given(jwtTokenProvider.getRefreshExpiration()).willReturn(1209600000L);

            // when
            TokenPair result = authService.reissue(oldRefreshToken);

            // then
            assertEquals("new-access-token", result.accessToken());
            assertEquals("new-refresh-token", result.refreshToken());
            verify(refreshTokenRepository).delete(stored);          // 기존 토큰 삭제 (RTR)
            verify(refreshTokenRepository).save(any(RefreshToken.class));  // 새 토큰 저장
        }

        @Test
        @DisplayName("DB 에 토큰이 없으면 AUTH_REFRESH_TOKEN_INVALID 를 던진다.")
        void 토큰_DB_미존재_예외() {
            // given
            String refreshToken = "untracked-token";
            given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.empty());

            // when & then
            UnauthorizedException ex = assertThrows(
                    UnauthorizedException.class,
                    () -> authService.reissue(refreshToken)
            );
            assertEquals(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID, ex.getErrorCode());
            verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("유저가 존재하지 않으면 AUTH_REFRESH_TOKEN_INVALID 를 던진다.")
        void 유저_없음_예외() {
            // given
            String refreshToken = "valid-refresh-token";
            RefreshToken stored = RefreshToken.create(1L, refreshToken, null);

            given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.of(stored));
            given(userRepository.findByUserIdAndDeletedAtIsNull(1L)).willReturn(Optional.empty());

            // when & then
            UnauthorizedException ex = assertThrows(
                    UnauthorizedException.class,
                    () -> authService.reissue(refreshToken)
            );
            assertEquals(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID, ex.getErrorCode());
        }

        @Test
        @DisplayName("계정이 잠금 상태면 USER_ACCOUNT_LOCKED 를 던진다.")
        void 유저_잠금_예외() {
            // given
            String refreshToken = "valid-refresh-token";
            User lockedUser = createTestUser(true);
            RefreshToken stored = RefreshToken.create(lockedUser.getUserId(), refreshToken, null);

            given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.of(stored));
            given(userRepository.findByUserIdAndDeletedAtIsNull(lockedUser.getUserId()))
                    .willReturn(Optional.of(lockedUser));

            // when & then
            ForbiddenException ex = assertThrows(
                    ForbiddenException.class,
                    () -> authService.reissue(refreshToken)
            );
            assertEquals(UserErrorCode.USER_ACCOUNT_LOCKED, ex.getErrorCode());
        }

        @Test
        @DisplayName("JWT 가 유효하지 않으면 validateRefreshToken 에서 예외가 던져진다.")
        void JWT_유효성_검증_실패_예외() {
            // given
            String invalidToken = "invalid-jwt";
            willThrow(new UnauthorizedException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID))
                    .given(jwtTokenProvider).validateRefreshToken(invalidToken);

            // when & then
            UnauthorizedException ex = assertThrows(
                    UnauthorizedException.class,
                    () -> authService.reissue(invalidToken)
            );
            assertEquals(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID, ex.getErrorCode());
            verify(refreshTokenRepository, never()).findByToken(anyString());
        }
    }
}