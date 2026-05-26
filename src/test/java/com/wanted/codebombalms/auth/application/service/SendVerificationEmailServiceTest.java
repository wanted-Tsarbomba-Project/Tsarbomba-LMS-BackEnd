package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.repository.EmailVerificationRepository;
import com.wanted.codebombalms.auth.domain.service.EmailSender;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.TooManyRequestsException;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendVerificationEmailService 단위 테스트")
class SendVerificationEmailServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailVerificationRepository emailVerificationRepository;
    @Mock private EmailSender emailSender;

    @InjectMocks
    private SendVerificationEmailService sendVerificationEmailService;

    private static final String EMAIL = "test@example.com";

    @Test
    @DisplayName("정상 발송 시 인증 코드를 Redis에 저장하고 이메일을 발송한다.")
    void 발송_성공() {
        // given
        given(userRepository.existsByEmail(EMAIL)).willReturn(false);
        given(emailVerificationRepository.isRecentlySent(EMAIL)).willReturn(false);
        given(emailVerificationRepository.incrementSendCount(EMAIL)).willReturn(1L);

        // when
        sendVerificationEmailService.sendVerificationCode(EMAIL);

        // then
        verify(emailVerificationRepository).saveCode(eq(EMAIL), anyString());
        verify(emailVerificationRepository).markRecentlySent(EMAIL);
        verify(emailSender).sendVerificationCode(eq(EMAIL), anyString());
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 ConflictException(USER_EMAIL_DUPLICATED)을 던진다.")
    void 이미_가입된_이메일_예외() {
        // given
        given(userRepository.existsByEmail(EMAIL)).willReturn(true);

        // when & then
        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> sendVerificationEmailService.sendVerificationCode(EMAIL)
        );
        assertEquals(UserErrorCode.USER_EMAIL_DUPLICATED, ex.getErrorCode());
        verify(emailSender, never()).sendVerificationCode(anyString(), anyString());
    }

    @Test
    @DisplayName("1분 이내 재발송 시 TooManyRequestsException(AUTH_EMAIL_SEND_TOO_MANY)을 던진다.")
    void 재발송_쿨다운_예외() {
        // given
        given(userRepository.existsByEmail(EMAIL)).willReturn(false);
        given(emailVerificationRepository.isRecentlySent(EMAIL)).willReturn(true);

        // when & then
        TooManyRequestsException ex = assertThrows(
                TooManyRequestsException.class,
                () -> sendVerificationEmailService.sendVerificationCode(EMAIL)
        );
        assertEquals(AuthErrorCode.AUTH_EMAIL_SEND_TOO_MANY, ex.getErrorCode());
        verify(emailSender, never()).sendVerificationCode(anyString(), anyString());
    }

    @Test
    @DisplayName("10분 내 6회 이상 발송 시 TooManyRequestsException(AUTH_EMAIL_SEND_TOO_MANY)을 던진다.")
    void 발송_횟수_초과_예외() {
        // given
        given(userRepository.existsByEmail(EMAIL)).willReturn(false);
        given(emailVerificationRepository.isRecentlySent(EMAIL)).willReturn(false);
        given(emailVerificationRepository.incrementSendCount(EMAIL)).willReturn(6L);   // 6번째

        // when & then
        TooManyRequestsException ex = assertThrows(
                TooManyRequestsException.class,
                () -> sendVerificationEmailService.sendVerificationCode(EMAIL)
        );
        assertEquals(AuthErrorCode.AUTH_EMAIL_SEND_TOO_MANY, ex.getErrorCode());
        verify(emailSender, never()).sendVerificationCode(anyString(), anyString());
    }
}