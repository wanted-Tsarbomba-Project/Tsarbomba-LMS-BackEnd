package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutService 단위 테스트")
class LogoutServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthSessionManager authSessionManager;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    @DisplayName("로그아웃 시 Refresh Token 삭제와 세션(sid) 폐기를 모두 수행한다.")
    void 로그아웃_성공() {
        // given
        Long userId = 1L;

        // when
        logoutService.logout(userId);

        // then
        verify(refreshTokenRepository).deleteByUserId(userId);
        verify(authSessionManager).close(userId);
    }
}