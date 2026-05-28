package com.wanted.codebombalms.learning.presentation.api.request;

import jakarta.validation.constraints.NotNull;

public record LectureProblemProgressRequest(
        @NotNull Long userId,
        Integer currentProblemNumber,
        boolean completed
) {
}
