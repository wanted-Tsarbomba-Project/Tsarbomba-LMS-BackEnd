package com.wanted.codebombalms.learning.infrastructure.course;

import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.infrastructure.persistence.CourseJpaEntity;
import com.wanted.codebombalms.course.infrastructure.persistence.SpringDataCourseRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.learning.application.port.LearningCoursePort;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.learning.domain.model.LearningCourse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningCourseAdapter implements LearningCoursePort {

    private final SpringDataCourseRepository courseRepository;

    @Override
    public List<LearningCourse> findActiveCourses() {
        return courseRepository.findByStatusAndDeletedAtIsNull(CourseStatus.ACTIVE)
                .stream()
                .map(this::toLearningCourse)
                .toList();
    }

    @Override
    public LearningCourse findActiveCourse(Long courseId) {
        return courseRepository.findByCourseIdAndStatusAndDeletedAtIsNull(courseId, CourseStatus.ACTIVE)
                .map(this::toLearningCourse)
                .orElseThrow(() -> new NotFoundException(LearningErrorCode.COURSE_NOT_FOUND));
    }

    private LearningCourse toLearningCourse(CourseJpaEntity course) {
        return new LearningCourse(course.getCourseId(), course.getTitle());
    }
}
