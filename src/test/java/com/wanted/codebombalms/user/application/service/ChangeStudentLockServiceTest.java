package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
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
@DisplayName("ChangeStudentLockService 단위 테스트")
class ChangeStudentLockServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private ChangeStudentLockService changeStudentLockService;

    @Test
    @DisplayName("잠금(locked=true) 시 계정을 잠그고 Refresh Token을 삭제한 뒤 저장한다.")
    void 계정_잠금() {
        // given
        User user = createUser(1L, false);
        given(userRepository.findByUserId(1L)).willReturn(Optional.of(user));

        // when
        changeStudentLockService.changeLock(1L, true);

        // then
        assertTrue(user.isLocked());
        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("해제(locked=false) 시 잠금을 풀고 Refresh Token은 삭제하지 않는다.")
    void 계정_해제() {
        // given
        User user = createUser(1L, true);
        given(userRepository.findByUserId(1L)).willReturn(Optional.of(user));

        // when
        changeStudentLockService.changeLock(1L, false);

        // then
        assertFalse(user.isLocked());
        verify(refreshTokenRepository, never()).deleteByUserId(any());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 NotFoundException(USER_NOT_FOUND)을 던지고 아무것도 저장/삭제하지 않는다.")
    void 회원_없음_예외() {
        // given
        given(userRepository.findByUserId(99L)).willReturn(Optional.empty());

        // when & then
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> changeStudentLockService.changeLock(99L, true)
        );
        assertEquals(UserErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        verify(refreshTokenRepository, never()).deleteByUserId(any());
        verify(userRepository, never()).save(any());
    }

    // ===== 테스트 헬퍼 =====

    private User createUser(Long userId, boolean isLocked) {
        return User.restore(
                userId, UserRole.STUDENT, "student@test.com", "ENCODED_PW",
                "김학생", "학생01", "010-1234-5678",
                AuthProvider.LOCAL, null,
                true, isLocked, null, null,
                LocalDateTime.now(), LocalDateTime.now(), null
        );
    }
}
