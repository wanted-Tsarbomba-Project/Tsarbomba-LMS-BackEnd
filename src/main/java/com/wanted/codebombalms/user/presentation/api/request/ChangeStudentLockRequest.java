package com.wanted.codebombalms.user.presentation.api.request;

import jakarta.validation.constraints.NotNull;

public record ChangeStudentLockRequest(

        @NotNull(message = "locked 값은 필수입니다.")
        Boolean locked
) {
}
