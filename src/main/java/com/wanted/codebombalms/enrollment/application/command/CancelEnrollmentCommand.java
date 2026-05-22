package com.wanted.codebombalms.enrollment.application.command;

public record CancelEnrollmentCommand(
        Long userId,
        Long enrollmentId
) {
}
