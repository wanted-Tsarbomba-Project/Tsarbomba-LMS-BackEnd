package com.wanted.codebombalms.lecture.presentation.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record LectureOrderUpdateRequest(
        @NotEmpty(message = "Lecture order list must not be empty.")
        List<@Valid LectureOrderItem> lectures
) {

    public record LectureOrderItem(
            @NotNull(message = "Lecture ID is required.")
            @Positive(message = "Lecture ID must be positive.")
            Long lectureId,

            @NotNull(message = "Lecture order is required.")
            @Positive(message = "Lecture order must be positive.")
            Integer lectureOrder
    ) {
    }
}
