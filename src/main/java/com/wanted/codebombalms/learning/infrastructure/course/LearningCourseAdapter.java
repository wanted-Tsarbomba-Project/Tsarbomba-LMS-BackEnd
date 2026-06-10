package com.wanted.codebombalms.learning.infrastructure.course;

import com.wanted.codebombalms.course.application.usecase.CourseQueryUseCase;
import com.wanted.codebombalms.course.domain.model.Course;
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

    private final CourseQueryUseCase courseQueryUseCase;

    @Override
    public List<LearningCourse> findActiveCourses() {
        return courseQueryUseCase.findAllCourses(null)
                .stream()
                .map(this::toLearningCourse)
                .toList();
    }

    @Override
    public LearningCourse findActiveCourse(Long courseId) {
        try {
            return toLearningCourse(courseQueryUseCase.findCourseById(courseId));
        } catch (NotFoundException e) {
            throw new NotFoundException(LearningErrorCode.COURSE_NOT_FOUND);
        }
    }

    private LearningCourse toLearningCourse(Course course) {
        return new LearningCourse(course.getCourseId(), course.getTitle());
    }
}
