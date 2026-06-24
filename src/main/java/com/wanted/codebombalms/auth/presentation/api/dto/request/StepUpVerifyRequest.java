package com.wanted.codebombalms.auth.presentation.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StepUpVerifyRequest(

        @NotBlank(message = "인증 코드는 필수입니다.")
        String code,

        boolean trustDevice
) {
}
