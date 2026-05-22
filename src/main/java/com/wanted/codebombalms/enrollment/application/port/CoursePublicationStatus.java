package com.wanted.codebombalms.enrollment.application.port;

public record CoursePublicationStatus(
        Long courseId,
        Long instructorId,
        String title,
        String description,
        String thumbnailUrl,
        boolean published
) {
}
