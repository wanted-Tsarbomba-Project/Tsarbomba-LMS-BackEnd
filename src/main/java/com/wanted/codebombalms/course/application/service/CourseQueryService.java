package com.wanted.codebombalms.course.application.service;

import com.wanted.codebombalms.course.application.usecase.CourseQueryUseCase;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.infrastructure.logging.aop.LogBusiness;
import com.wanted.codebombalms.global.infrastructure.logging.aop.LogPerformance;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseQueryService implements CourseQueryUseCase {

    private static final Logger log = LoggerFactory.getLogger(CourseQueryService.class);

    private final CourseRepository courseRepository;

    @LogBusiness
    @LogPerformance
    @Override
    public List<Course> findAllCourses(Long courseCategoryId) {
        log.info("[CourseQueryService] find active courses");

        List<Course> courses = courseCategoryId == null
                ? courseRepository.findByStatusAndDeletedAtIsNull(CourseStatus.ACTIVE)
                : courseRepository.findByCourseCategoryIdAndStatusAndDeletedAtIsNull(
                        courseCategoryId,
                        CourseStatus.ACTIVE
                );

        log.info("[CourseQueryService] found active courses - count: {}", courses.size());

        return courses;
    }

    @Override
    public Course findCourseById(Long courseId) {
        log.info("[CourseQueryService] find active course - courseId: {}", courseId);

        var course = courseRepository.findByCourseIdAndStatusAndDeletedAtIsNull(courseId, CourseStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        log.info("[CourseQueryService] found active course - courseId: {}", courseId);

        return course;
    }
}
