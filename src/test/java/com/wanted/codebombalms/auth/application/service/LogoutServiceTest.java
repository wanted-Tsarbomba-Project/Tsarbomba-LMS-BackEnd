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

    @InjectMocks
    private LogoutService logoutService;

    @Test
    @DisplayName("로그아웃 시 해당 유저의 Refresh Token을 전부 삭제한다.")
    void 로그아웃_성공() {
        // given
        Long userId = 1L;

        // when
        logoutService.logout(userId);

        // then
        verify(refreshTokenRepository).deleteByUserId(userId);
    }
}