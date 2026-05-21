package com.wanted.codebombalms.domain.course.presentation.api.response;

import com.wanted.codebombalms.domain.course.application.result.CourseSummaryResult;
import com.wanted.codebombalms.domain.course.domain.model.CourseStatus;

public record CourseResponse(
        Long courseId,
        Long instructorId,
        String title,
        String thumbnailUrl,
        CourseStatus status
) {

    public static CourseResponse from(CourseSummaryResult result) {
        return new CourseResponse(
                result.courseId(),
                result.instructorId(),
                result.title(),
                result.thumbnailUrl(),
                result.status()
        );
    }
}
