package com.wanted.codebombalms.user.presentation.api.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMyInfoRequest(

        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        @Pattern(
                regexp = "^(?!\\s*$).+",
                message = "닉네임은 공백일 수 없습니다."
        )
        String nickname,

        @Pattern(
                regexp = "^01[0-9]-\\d{3,4}-\\d{4}$",
                message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)"
        )
        String phone
) {
}
