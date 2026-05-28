package com.wanted.codebombalms.course.presentation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseCreateRequest(
        @NotNull(message = "Course category ID is required.")
        Long courseCategoryId,

        @NotBlank(message = "Course title is required.")
        @Size(max = 100, message = "Course title must be 100 characters or fewer.")
        String title,

        String description,

        @Size(max = 500, message = "Thumbnail URL must be 500 characters or fewer.")
        String thumbnailUrl
) {
}
