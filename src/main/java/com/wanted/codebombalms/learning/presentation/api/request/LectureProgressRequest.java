package com.wanted.codebombalms.learning.presentation.api.request;

public record LectureProgressRequest(
        Long userId,
        boolean completed
) {
}
