package com.wanted.codebombalms.course.presentation.api.response;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;

public record CourseResponse(
        Long courseId,
        Long instructorId,
        Long courseCategoryId,
        String courseCategoryName,
        String title,
        String thumbnailUrl,
        CourseStatus status
) {

    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getCourseId(),
                course.getInstructorId(),
                course.getCourseCategoryId(),
                course.getCourseCategoryName(),
                course.getTitle(),
                course.getThumbnailUrl(),
                course.getStatus()
        );
    }
}
