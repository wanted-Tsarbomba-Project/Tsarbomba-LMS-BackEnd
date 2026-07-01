package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawUserService 단위 테스트")
class WithdrawUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;

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

    private User socialUser() {
        return User.restore(
                2L, UserRole.STUDENT, "g01@gmail.com", null,     // 소셜은 비밀번호 없음
                "김구글", "구글01", "010-2222-3333",
                AuthProvider.GOOGLE, "google-sub-123",
                true, false, null, null,
                LocalDateTime.now(), LocalDateTime.now(), null
        );
    }

    @Test
    @DisplayName("[LOCAL] 비밀번호가 일치하면 softDelete 후 저장하고 Refresh Token을 전부 삭제한다.")
    void 탈퇴_성공() {
        // given
        User user = activeUser();
        given(userRepository.findByUserId(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Test1234!", "ENCODED_PW")).willReturn(true);

        // when
        withdrawUserService.withdraw(1L, "Test1234!", null);

        // then — softDelete 반영된 User가 저장되는지 캡처해서 검증
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertTrue(captor.getValue().isDeleted(), "deleted_at 이 채워져야 한다");
        verify(refreshTokenRepository).deleteByUserId(1L);
    }

    @Test
    @DisplayName("[LOCAL] 비밀번호가 불일치하면 ValidationException(AUTH_PASSWORD_MISMATCH)을 던지고, 저장/RT삭제는 일어나지 않는다.")
    void 비밀번호_불일치_예외() {
        // given
        User user = activeUser();
        given(userRepository.findByUserId(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong", "ENCODED_PW")).willReturn(false);

        // when & then
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> withdrawUserService.withdraw(1L, "wrong", null)
        );
        assertEquals(AuthErrorCode.AUTH_PASSWORD_MISMATCH, ex.getErrorCode());

        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteByUserId(anyLong());
    }

    @Test
    @DisplayName("[소셜] 확인 문구가 '탈퇴하겠습니다' 와 일치하면 비밀번호 검증 없이 탈퇴에 성공한다.")
    void 소셜_탈퇴_성공() {
        // given
        User user = socialUser();
        given(userRepository.findByUserId(2L)).willReturn(Optional.of(user));

        // when
        withdrawUserService.withdraw(2L, null, "탈퇴하겠습니다");

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertTrue(captor.getValue().isDeleted(), "deleted_at 이 채워져야 한다");
        verify(refreshTokenRepository).deleteByUserId(2L);
    }

    @Test
    @DisplayName("[소셜] 확인 문구가 불일치하면 ValidationException(USER_WITHDRAW_CONFIRM_MISMATCH)을 던지고, 저장/RT삭제는 일어나지 않는다.")
    void 소셜_확인문구_불일치_예외() {
        // given
        User user = socialUser();
        given(userRepository.findByUserId(2L)).willReturn(Optional.of(user));

        // when & then
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> withdrawUserService.withdraw(2L, null, "탈퇴할래요")
        );
        assertEquals(UserErrorCode.USER_WITHDRAW_CONFIRM_MISMATCH, ex.getErrorCode());

        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteByUserId(anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 NotFoundException(USER_NOT_FOUND)을 던진다.")
    void 회원_없음_예외() {
        // given
        given(userRepository.findByUserId(99L)).willReturn(Optional.empty());

        // when & then
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> withdrawUserService.withdraw(99L, "Test1234!", null)
        );
        assertEquals(UserErrorCode.USER_NOT_FOUND, ex.getErrorCode());

        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).deleteByUserId(anyLong());
    }
}