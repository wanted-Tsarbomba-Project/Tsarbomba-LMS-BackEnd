package com.wanted.codebombalms.lecture.infrastructure.course;

import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.lecture.application.port.CourseCatalogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseCatalogAdapter implements CourseCatalogPort {

    private final CourseRepository courseRepository;

    @Override
    public Course findCourse(Long courseId) {
        return courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));
    }
}
