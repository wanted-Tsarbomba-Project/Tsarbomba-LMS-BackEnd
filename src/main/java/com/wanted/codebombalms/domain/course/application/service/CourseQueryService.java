package com.wanted.codebombalms.domain.course.application.service;

import com.wanted.codebombalms.domain.course.application.usecase.CourseQueryUseCase;
import com.wanted.codebombalms.domain.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.course.domain.model.CourseStatus;
import com.wanted.codebombalms.domain.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.domain.course.presentation.api.response.CourseDetailResponse;
import com.wanted.codebombalms.domain.course.presentation.api.response.CourseResponse;
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
    public List<CourseResponse> findAllCourses() {

        log.info("[CourseQueryService] 강좌 목록 조회 시작");

        List<CourseResponse> courses = courseRepository.findByStatusAndDeletedAtIsNull(CourseStatus.ACTIVE)
                .stream()
                .map(CourseResponse::from)
                .toList();

        log.info("[CourseQueryService] 강좌 목록 조회 완료 - count: {}", courses.size());

        return courses;
    }

    @Override
    public CourseDetailResponse findCourseById(Long courseId) {

        log.info("[CourseQueryService] 강좌 상세 조회 시작 - courseId: {}", courseId);

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        log.info("[CourseQueryService] 강좌 상세 조회 완료 - courseId: {}", courseId);

        return CourseDetailResponse.from(course);
    }
}
