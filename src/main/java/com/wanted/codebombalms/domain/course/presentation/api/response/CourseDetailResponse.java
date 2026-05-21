package com.wanted.codebombalms.domain.course.presentation.api.response;

import com.wanted.codebombalms.domain.course.application.result.CourseDetailResult;
import com.wanted.codebombalms.domain.course.domain.model.CourseStatus;

import java.time.LocalDateTime;

public record CourseDetailResponse(
        Long courseId,
        Long instructorId,
        String title,
        String description,
        String thumbnailUrl,
        CourseStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CourseDetailResponse from(CourseDetailResult result) {
        return new CourseDetailResponse(
                result.courseId(),
                result.instructorId(),
                result.title(),
                result.description(),
                result.thumbnailUrl(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
