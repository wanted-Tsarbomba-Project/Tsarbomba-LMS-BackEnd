package com.wanted.codebombalms.auth.presentation.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OAuthCompleteRequest(

        @Schema(description = "닉네임", example = "gildong", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 30, message = "닉네임은 30자 이하여야 합니다.")
        String nickname,

        @Schema(description = "전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(
                regexp = "^01[0-9]-\\d{3,4}-\\d{4}$",
                message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)"
        )
        String phone,

        @Schema(description = "이용약관 동의 (필수, 반드시 true)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean termsOfServiceAgreed,

        @Schema(description = "개인정보 수집·이용 동의 (필수, 반드시 true)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean privacyPolicyAgreed
) {
}