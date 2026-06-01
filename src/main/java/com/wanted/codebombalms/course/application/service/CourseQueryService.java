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
    public List<Course> findAllCoursesForOperator(Long courseCategoryId) {
        log.info("[CourseQueryService] find operator courses");

        List<Course> courses = courseCategoryId == null
                ? courseRepository.findByDeletedAtIsNull()
                : courseRepository.findByCourseCategoryIdAndDeletedAtIsNull(courseCategoryId);

        log.info("[CourseQueryService] found operator courses - count: {}", courses.size());

        return courses;
    }

    @Override
    public List<Course> findCoursesByInstructor(Long instructorId) {
        log.info("[CourseQueryService] find instructor courses - instructorId: {}", instructorId);

        List<Course> courses = courseRepository.findByInstructorIdAndDeletedAtIsNull(instructorId);

        log.info("[CourseQueryService] found instructor courses - instructorId: {}, count: {}",
                instructorId,
                courses.size()
        );

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

    @Override
    public Course findCourseByIdForOperator(Long courseId) {
        log.info("[CourseQueryService] find operator course - courseId: {}", courseId);

        var course = courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        log.info("[CourseQueryService] found operator course - courseId: {}", courseId);

        return course;
    }
}
