package com.wanted.codebombalms.user.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyPasswordRequest(

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
