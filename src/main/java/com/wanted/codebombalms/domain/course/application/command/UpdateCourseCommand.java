package com.wanted.codebombalms.domain.course.application.command;

import com.wanted.codebombalms.domain.course.domain.model.CourseStatus;

public record UpdateCourseCommand(
        Long courseId,
        String title,
        String description,
        String thumbnailUrl,
        CourseStatus status
) {
}
