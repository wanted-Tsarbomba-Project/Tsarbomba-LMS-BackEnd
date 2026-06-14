package com.wanted.codebombalms.user.presentation.api.request;

import jakarta.validation.constraints.Pattern;

public record UpdateMyInfoRequest(

        String nickname,

        @Pattern(
                regexp = "^01[0-9]-\\d{3,4}-\\d{4}$",
                message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)"
        )
        String phone
) {
}
