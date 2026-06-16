package com.wanted.codebombalms.learning.presentation.api.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LectureProgressRequest(
        @NotNull
        @Min(0)
        Integer lastPositionSec,

        @Positive
        Integer durationSec,

        @NotNull
        @Min(0)
        Integer watchedDeltaSec
) {
}
