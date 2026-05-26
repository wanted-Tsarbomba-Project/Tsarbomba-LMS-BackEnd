package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.command.SignupCommand;
import com.wanted.codebombalms.auth.domain.repository.EmailVerificationRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SignupService 단위 테스트")
class SignupServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailVerificationRepository emailVerificationRepository;

    @InjectMocks
    private SignupService signupService;

    private SignupCommand command;

    @BeforeEach
    void setUp() {
        command = new SignupCommand(
                "test@example.com",
                "Test1234!",
                "Test1234!",
                "홍길동",
                "길동이",
                "010-1234-5678"
        );
    }

    @Test
    @DisplayName("정상 회원가입 시 User를 저장하고 userId를 반환한다.")
    void 회원가입_성공() {
        // given
        given(emailVerificationRepository.isVerified("test@example.com")).willReturn(true);
        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(userRepository.existsByNickname("길동이")).willReturn(false);
        given(passwordEncoder.encode("Test1234!")).willReturn("encoded_password");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return User.restore(
                    1L, u.getRole(), u.getEmail(), u.getPassword(),
                    u.getName(), u.getNickname(), u.getPhone(),
                    u.getProvider(), null, false, false, null, null,
                    null, null, null
            );
        });

        // when
        Long userId = signupService.signup(command);

        // then
        assertEquals(1L, userId);
        verify(userRepository).save(any(User.class));
        verify(emailVerificationRepository).clearVerified("test@example.com");
    }

    @Test
    @DisplayName("이메일 인증을 완료하지 않으면 ValidationException(USER_EMAIL_NOT_VERIFIED)을 던진다.")
    void 이메일_미인증_예외() {
        // given
        given(emailVerificationRepository.isVerified("test@example.com")).willReturn(false);

        // when & then
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> signupService.signup(command)
        );
        assertEquals(UserErrorCode.USER_EMAIL_NOT_VERIFIED, ex.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호와 비밀번호 확인이 일치하지 않으면 ValidationException(USER_PASSWORD_CONFIRM_MISMATCH)을 던진다.")
    void 비밀번호_확인_불일치_예외() {
        // given
        SignupCommand mismatch = new SignupCommand(
                "test@example.com",
                "Test1234!",
                "DifferentPassword5!",     // 일치하지 않음
                "홍길동",
                "길동이",
                "010-1234-5678"
        );
        given(emailVerificationRepository.isVerified("test@example.com")).willReturn(true);

        // when & then
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> signupService.signup(mismatch)
        );
        assertEquals(UserErrorCode.USER_PASSWORD_CONFIRM_MISMATCH, ex.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("이메일이 중복되면 ConflictException(USER_EMAIL_DUPLICATED)을 던진다.")
    void 이메일_중복_예외() {
        // given
        given(emailVerificationRepository.isVerified("test@example.com")).willReturn(true);
        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        // when & then
        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> signupService.signup(command)
        );
        assertEquals(UserErrorCode.USER_EMAIL_DUPLICATED, ex.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("닉네임이 중복되면 ConflictException(USER_NICKNAME_DUPLICATED)을 던진다.")
    void 닉네임_중복_예외() {
        // given
        given(emailVerificationRepository.isVerified("test@example.com")).willReturn(true);
        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(userRepository.existsByNickname("길동이")).willReturn(true);

        // when & then
        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> signupService.signup(command)
        );
        assertEquals(UserErrorCode.USER_NICKNAME_DUPLICATED, ex.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 시 비밀번호를 BCrypt로 인코딩한 후 저장한다.")
    void 비밀번호_BCrypt_인코딩() {
        // given
        given(emailVerificationRepository.isVerified("test@example.com")).willReturn(true);
        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(userRepository.existsByNickname("길동이")).willReturn(false);
        given(passwordEncoder.encode("Test1234!")).willReturn("encoded_password");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        signupService.signup(command);

        // then
        verify(passwordEncoder).encode("Test1234!");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encoded_password")
        ));
    }
}