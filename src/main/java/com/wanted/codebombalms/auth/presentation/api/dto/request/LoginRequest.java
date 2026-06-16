package com.wanted.codebombalms.auth.presentation.api.dto.request;

import com.wanted.codebombalms.auth.application.command.LoginCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {

    public LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }
}
