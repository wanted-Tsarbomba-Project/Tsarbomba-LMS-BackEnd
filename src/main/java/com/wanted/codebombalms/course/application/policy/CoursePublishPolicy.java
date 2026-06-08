package com.wanted.codebombalms.course.application.policy;

import com.wanted.codebombalms.course.application.port.LectureCatalogPort;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoursePublishPolicy {

    private final LectureCatalogPort lectureCatalogPort;

    public void validate(Course course) {
        if (course.getStatus() != CourseStatus.DRAFT) {
            throw new ValidationException(CourseErrorCode.COURSE_NOT_PUBLISHABLE_STATUS);
        }

        validateActivationRequirements(course);
    }

    public void validateActivationRequirements(Course course) {
        if (!lectureCatalogPort.existsLectureInCourse(course.getCourseId())) {
            throw new ValidationException(CourseErrorCode.COURSE_LECTURE_REQUIRED);
        }
    }
}
