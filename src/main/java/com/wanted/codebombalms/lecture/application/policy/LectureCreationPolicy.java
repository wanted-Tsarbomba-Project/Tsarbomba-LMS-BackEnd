package com.wanted.codebombalms.lecture.application.policy;

import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class LectureCreationPolicy {

    public void validate(Course course) {
        if (course.getStatus() == CourseStatus.DELETED) {
            throw new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND);
        }
    }
}
