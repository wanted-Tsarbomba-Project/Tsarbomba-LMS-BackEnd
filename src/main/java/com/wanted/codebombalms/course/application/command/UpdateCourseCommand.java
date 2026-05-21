package com.wanted.codebombalms.course.application.command;

import com.wanted.codebombalms.course.domain.model.CourseStatus;

public record UpdateCourseCommand(
        Long courseId,
        String title,
        String description,
        String thumbnailUrl,
        CourseStatus status
) {
}
