package com.wanted.codebombalms.auth.presentation.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StepUpVerifyRequest(

        @NotBlank(message = "인증 코드는 필수입니다.")
        @Pattern(regexp = "\\d{6}", message = "인증 코드는 6자리 숫자여야 합니다.")
        String code,

        boolean trustDevice
) {
}
