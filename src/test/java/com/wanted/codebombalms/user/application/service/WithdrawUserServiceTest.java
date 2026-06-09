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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawUserService 단위 테스트")
class WithdrawUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private WithdrawUserService withdrawUserService;

    private User activeUser() {
        return User.restore(
                1L, UserRole.STUDENT, "u01@test.com", "ENCODED_PW",
                "김학생", "학생01", "010-1234-5678",
                AuthProvider.LOCAL, null,
                true, false, null, null,
                LocalDateTime.now(), LocalDateTime.now(), null
        );
    }

    @Test
    @DisplayName("탈퇴 시 softDelete 처리 후 저장하고, Refresh Token을 전부 삭제한다.")
    void 탈퇴_성공() {
        // given
        User user = activeUser();
        given(userRepository.findByUserId(1L)).willReturn(Optional.of(user));

        // when
        withdrawUserService.withdraw(1L);

        // then — softDelete 반영된 User가 저장되는지 캡처해서 검증
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertTrue(captor.getValue().isDeleted(), "deleted_at 이 채워져야 한다");

        // RT 전체 삭제 호출 검증
        verify(refreshTokenRepository).deleteByUserId(1L);
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 NotFoundException(USER_NOT_FOUND)을 던지고, 저장/RT삭제는 일어나지 않는다.")
    void 회원_없음_예외() {
        // given
        given(userRepository.findByUserId(99L)).willReturn(Optional.empty());

        // when & then
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> withdrawUserService.withdraw(99L)
        );
        assertEquals(UserErrorCode.USER_NOT_FOUND, ex.getErrorCode());

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(refreshTokenRepository, never()).deleteByUserId(org.mockito.ArgumentMatchers.anyLong());
    }
}
