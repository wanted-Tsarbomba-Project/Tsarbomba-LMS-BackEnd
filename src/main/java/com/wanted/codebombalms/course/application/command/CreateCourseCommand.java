package com.wanted.codebombalms.course.application.command;

public record CreateCourseCommand(
        Long instructorId,
        Long courseCategoryId,
        String title,
        String description,
        String thumbnailUrl
) {
}
