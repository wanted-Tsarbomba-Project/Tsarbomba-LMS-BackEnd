package com.wanted.codebombalms.user.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FindEmailRequest(

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
        String name,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Size(max = 20, message = "전화번호 형식이 올바르지 않습니다.")
        String phone
) {
}