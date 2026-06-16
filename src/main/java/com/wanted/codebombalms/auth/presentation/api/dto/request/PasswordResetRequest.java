package com.wanted.codebombalms.auth.presentation.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetRequest(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "재설정 코드는 필수입니다.")
        @Pattern(regexp = "^\\d{6}$", message = "재설정 코드는 6자리 숫자입니다.")
        String code,

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+={}\\[\\]:;\"'<>,.?/~`\\\\|-]).{8,}$",
                message = "비밀번호는 8자 이상, 영문/숫자/특수문자를 모두 포함해야 합니다."
        )
        String newPassword
) {
}
