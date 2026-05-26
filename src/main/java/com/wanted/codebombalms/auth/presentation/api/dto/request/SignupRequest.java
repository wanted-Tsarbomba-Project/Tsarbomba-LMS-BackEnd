package com.wanted.codebombalms.auth.presentation.api.dto.request;

import com.wanted.codebombalms.auth.application.command.SignupCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+={}\\[\\]:;\"'<>,.?/~`\\\\|-]).{8,}$",
                message = "비밀번호는 8자 이상, 영문/숫자/특수문자를 모두 포함해야 합니다."
        )
        String password,

        @NotBlank(message = "비밀번호 확인은 필수입니다.")
        String passwordConfirm,

        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(
                regexp = "^01[0-9]-\\d{3,4}-\\d{4}$",
                message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)"
        )
        String phone
) {

    public SignupCommand toCommand() {
        return new SignupCommand(email, password, passwordConfirm, name, nickname, phone);
    }
}