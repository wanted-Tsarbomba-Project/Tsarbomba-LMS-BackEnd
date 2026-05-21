package com.wanted.codebombalms.course.presentation.api.response;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;

public record CourseResponse(
        Long courseId,
        Long instructorId,
        String title,
        String thumbnailUrl,
        CourseStatus status
) {

    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getCourseId(),
                course.getInstructorId(),
                course.getTitle(),
                course.getThumbnailUrl(),
                course.getStatus()
        );
    }
}
