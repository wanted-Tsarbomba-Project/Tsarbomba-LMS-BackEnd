package com.wanted.codebombalms.domain.course.application.command;

public record CreateCourseCommand(
        Long instructorId,
        String title,
        String description,
        String thumbnailUrl
) {
}
