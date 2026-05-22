package com.wanted.codebombalms.enrollment.presentation.api.request;

import jakarta.validation.constraints.NotNull;

public record EnrollCourseRequest(
        @NotNull(message = "회원 ID는 필수입니다.")
        Long userId,

        @NotNull(message = "강좌 ID는 필수입니다.")
        Long courseId
) {
}
