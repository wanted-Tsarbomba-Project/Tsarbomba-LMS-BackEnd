package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.dto.TokenPair;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenReissueService 단위 테스트")
class TokenReissueServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private TokenReissueService tokenReissueService;

    private static final String OLD_TOKEN = "OLD_REFRESH_TOKEN";

    @BeforeEach
    void setUp() {
        // validateRefreshToken 은 void — 기본 mock 동작이 "아무것도 안 함" 이라 stub 불필요
    }

    @Test
    @DisplayName("정상 재발급 시 새 TokenPair를 반환하고 기존 RT를 삭제 후 새 RT를 저장한다 (RTR).")
    void 재발급_성공() {
        // given
        RefreshToken stored = createValidRefreshToken(1L, OLD_TOKEN);
        User user = createUser(1L, false);

        given(refreshTokenRepository.findByToken(OLD_TOKEN)).willReturn(Optional.of(stored));
        given(userRepository.findByUserId(1L)).willReturn(Optional.of(user));
        given(jwtTokenProvider.generateAccessToken(1L, UserRole.STUDENT)).willReturn("NEW_ACCESS");
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("NEW_REFRESH");
        given(jwtTokenProvider.getRefreshExpiration()).willReturn(1209600000L);

        // when
        TokenPair pair = tokenReissueService.reissue(OLD_TOKEN);

        // then
        assertEquals("NEW_ACCESS", pair.accessToken());
        assertEquals("NEW_REFRESH", pair.refreshToken());

        // RTR — 삭제 후 저장 순서
        var inOrder = inOrder(refreshTokenRepository);
        inOrder.verify(refreshTokenRepository).deleteByUserId(1L);
        inOrder.verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("DB에 없는 Refresh Token이면 UnauthorizedException(REFRESH_TOKEN_INVALID)을 던진다.")
    void DB_토큰_없음_예외() {
        // given
        given(refreshTokenRepository.findByToken(OLD_TOKEN)).willReturn(Optional.empty());

        // when & then
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> tokenReissueService.reissue(OLD_TOKEN)
        );
        assertEquals(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID, ex.getErrorCode());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("DB의 Refresh Token이 만료되었으면 UnauthorizedException(REFRESH_TOKEN_EXPIRED)을 던진다.")
    void DB_토큰_만료_예외() {
        // given
        RefreshToken expired = createExpiredRefreshToken(1L, OLD_TOKEN);
        given(refreshTokenRepository.findByToken(OLD_TOKEN)).willReturn(Optional.of(expired));

        // when & then
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> tokenReissueService.reissue(OLD_TOKEN)
        );
        assertEquals(AuthErrorCode.AUTH_REFRESH_TOKEN_EXPIRED, ex.getErrorCode());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("유저가 존재하지 않으면 NotFoundException(USER_NOT_FOUND)을 던진다.")
    void 유저_없음_예외() {
        // given
        RefreshToken stored = createValidRefreshToken(1L, OLD_TOKEN);
        given(refreshTokenRepository.findByToken(OLD_TOKEN)).willReturn(Optional.of(stored));
        given(userRepository.findByUserId(1L)).willReturn(Optional.empty());

        // when & then
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> tokenReissueService.reissue(OLD_TOKEN)
        );
        assertEquals(UserErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("잠긴 계정이면 ForbiddenException(USER_ACCOUNT_LOCKED)을 던진다.")
    void 계정_잠금_예외() {
        // given
        RefreshToken stored = createValidRefreshToken(1L, OLD_TOKEN);
        User lockedUser = createUser(1L, true);
        given(refreshTokenRepository.findByToken(OLD_TOKEN)).willReturn(Optional.of(stored));
        given(userRepository.findByUserId(1L)).willReturn(Optional.of(lockedUser));

        // when & then
        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> tokenReissueService.reissue(OLD_TOKEN)
        );
        assertEquals(UserErrorCode.USER_ACCOUNT_LOCKED, ex.getErrorCode());
        verify(refreshTokenRepository, never()).save(any());
    }

    // ===== 테스트 헬퍼 =====

    private RefreshToken createValidRefreshToken(Long userId, String token) {
        return RefreshToken.restore(
                1L,
                userId,
                token,
                LocalDateTime.now().plusHours(1),    // 미래 = 유효
                LocalDateTime.now().minusMinutes(10)
        );
    }

    private RefreshToken createExpiredRefreshToken(Long userId, String token) {
        return RefreshToken.restore(
                1L,
                userId,
                token,
                LocalDateTime.now().minusHours(1),   // 과거 = 만료
                LocalDateTime.now().minusDays(7)
        );
    }

    private User createUser(Long userId, boolean isLocked) {
        return User.restore(
                userId,
                UserRole.STUDENT,
                "test@example.com",
                "ENCODED_PW",
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