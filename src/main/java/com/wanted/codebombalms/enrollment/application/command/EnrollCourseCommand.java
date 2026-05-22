package com.wanted.codebombalms.enrollment.application.command;

public record EnrollCourseCommand(
        Long userId,
        Long courseId
) {
}
