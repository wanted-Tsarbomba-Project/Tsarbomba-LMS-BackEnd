package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.repository.EmailVerificationRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerifyEmailCodeService 단위 테스트")
class VerifyEmailCodeServiceTest {

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @InjectMocks
    private VerifyEmailCodeService verifyEmailCodeService;

    private static final String EMAIL = "test@example.com";
    private static final String VALID_CODE = "382947";

    @Test
    @DisplayName("정상 검증 시 코드를 삭제하고 인증 완료 플래그를 저장한다.")
    void 검증_성공() {
        // given
        given(emailVerificationRepository.findCode(EMAIL)).willReturn(Optional.of(VALID_CODE));

        // when
        verifyEmailCodeService.verifyCode(EMAIL, VALID_CODE);

        // then — 삭제 후 완료 플래그 저장 순서
        var inOrder = inOrder(emailVerificationRepository);
        inOrder.verify(emailVerificationRepository).deleteCode(EMAIL);
        inOrder.verify(emailVerificationRepository).markVerified(EMAIL);
    }

    @Test
    @DisplayName("Redis에 코드가 없으면 ValidationException(AUTH_CODE_EXPIRED)을 던진다.")
    void 코드_만료_예외() {
        // given
        given(emailVerificationRepository.findCode(EMAIL)).willReturn(Optional.empty());

        // when & then
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> verifyEmailCodeService.verifyCode(EMAIL, VALID_CODE)
        );
        assertEquals(AuthErrorCode.AUTH_CODE_EXPIRED, ex.getErrorCode());
        verify(emailVerificationRepository, never()).deleteCode(any());
        verify(emailVerificationRepository, never()).markVerified(any());
    }

    @Test
    @DisplayName("입력한 코드가 저장된 코드와 다르면 ValidationException(AUTH_CODE_INVALID)을 던진다.")
    void 코드_불일치_예외() {
        // given
        given(emailVerificationRepository.findCode(EMAIL)).willReturn(Optional.of(VALID_CODE));

        // when & then
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> verifyEmailCodeService.verifyCode(EMAIL, "999999")   // 다른 코드 입력
        );
        assertEquals(AuthErrorCode.AUTH_CODE_INVALID, ex.getErrorCode());
        verify(emailVerificationRepository, never()).deleteCode(any());
        verify(emailVerificationRepository, never()).markVerified(any());
    }
}