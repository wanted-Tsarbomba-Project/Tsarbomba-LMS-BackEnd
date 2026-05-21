package com.wanted.codebombalms.domain.course.application.result;

import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.course.domain.model.CourseStatus;

import java.time.LocalDateTime;

public record CourseDetailResult(
        Long courseId,
        Long instructorId,
        String title,
        String description,
        String thumbnailUrl,
        CourseStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CourseDetailResult from(Course course) {
        return new CourseDetailResult(
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
