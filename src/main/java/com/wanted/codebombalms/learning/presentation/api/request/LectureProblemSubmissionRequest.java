package com.wanted.codebombalms.learning.presentation.api.request;

import jakarta.validation.constraints.NotBlank;

public record LectureProblemSubmissionRequest(
        @NotBlank String submittedAnswer
) {
}
