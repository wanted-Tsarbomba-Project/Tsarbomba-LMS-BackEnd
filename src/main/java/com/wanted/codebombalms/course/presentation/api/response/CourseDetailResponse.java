package com.wanted.codebombalms.course.presentation.api.response;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;

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

    public static CourseDetailResponse from(Course course) {
        return new CourseDetailResponse(
                course.getCourseId(),
                course.getInstructorId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getStatus(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
