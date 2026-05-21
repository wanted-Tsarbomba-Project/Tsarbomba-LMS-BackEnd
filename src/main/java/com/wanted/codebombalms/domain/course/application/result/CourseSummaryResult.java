package com.wanted.codebombalms.domain.course.application.result;

import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.course.domain.model.CourseStatus;

public record CourseSummaryResult(
        Long courseId,
        Long instructorId,
        String title,
        String thumbnailUrl,
        CourseStatus status
) {

    public static CourseSummaryResult from(Course course) {
        return new CourseSummaryResult(
                course.getCourseId(),
                course.getInstructorId(),
                course.getTitle(),
                course.getThumbnailUrl(),
                course.getStatus()
        );
    }
}
