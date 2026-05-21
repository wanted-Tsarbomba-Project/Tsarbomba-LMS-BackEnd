package com.wanted.codebombalms.course.application.command;

public record CreateCourseCommand(
        Long instructorId,
        String title,
        String description,
        String thumbnailUrl
) {
}
