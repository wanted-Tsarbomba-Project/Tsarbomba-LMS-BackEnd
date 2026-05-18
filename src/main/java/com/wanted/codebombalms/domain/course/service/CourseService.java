package com.wanted.codebombalms.domain.course.service;

import com.wanted.codebombalms.domain.course.dto.request.CourseCreateRequest;
import com.wanted.codebombalms.domain.course.dto.request.CourseUpdateRequest;
import com.wanted.codebombalms.domain.course.dto.response.CourseDetailResponse;
import com.wanted.codebombalms.domain.course.dto.response.CourseResponse;
import com.wanted.codebombalms.domain.course.entity.Course;
import com.wanted.codebombalms.domain.course.exception.CourseErrorCode;
import com.wanted.codebombalms.global.error.exception.NotFoundException;
import com.wanted.codebombalms.domain.course.repository.CourseRepository;
import com.wanted.codebombalms.global.logging.aop.LogBusiness;
import com.wanted.codebombalms.global.logging.aop.LogPerformance;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;

    /**
     * 강좌 등록
     */
    @LogBusiness
    @LogPerformance
    @Transactional
    public CourseDetailResponse createCourse(CourseCreateRequest request) {

        log.info("[CourseService] 강좌 등록 시작 - title: {}", request.getTitle());

        Course course = new Course();
        course.setInstructorId(request.getInstructorId());
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setStatus(request.getStatus());

        Course savedCourse = courseRepository.save(course);

        log.info("[CourseService] 강좌 등록 완료 - courseId: {}", savedCourse.getCourseId());

        return CourseDetailResponse.from(savedCourse);
    }

    /**
     * 강좌 목록 조회
     */
    @LogBusiness
    @LogPerformance
    public List<CourseResponse> findAllCourses() {

        log.info("[CourseService] 강좌 목록 조회 시작");

        List<CourseResponse> courses = courseRepository.findByDeletedAtIsNull()
                .stream()
                .map(CourseResponse::from)
                .toList();

        log.info("[CourseService] 강좌 목록 조회 완료 - count: {}", courses.size());

        return courses;
    }

    /**
     * 강좌 상세 조회
     */
    public CourseDetailResponse findCourseById(Long courseId) {

        log.info("[CourseService] 강좌 상세 조회 시작 - courseId: {}", courseId);

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        log.info("[CourseService] 강좌 상세 조회 완료 - courseId: {}", courseId);

        return CourseDetailResponse.from(course);
    }

    /**
     * 강좌 수정
     */
    @Transactional
    public CourseDetailResponse updateCourse(Long courseId, CourseUpdateRequest request) {

        log.info("[CourseService] 강좌 수정 시작 - courseId: {}", courseId);

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        if (request.getTitle() != null) {
            course.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }

        if (request.getThumbnailUrl() != null) {
            course.setThumbnailUrl(request.getThumbnailUrl());
        }

        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }

        log.info("[CourseService] 강좌 수정 완료 - courseId: {}", courseId);

        return CourseDetailResponse.from(course);
    }

    /**
     * 강좌 삭제
     */
    @Transactional
    public void deleteCourse(Long courseId) {

        log.info("[CourseService] 강좌 삭제 시작 - courseId: {}", courseId);

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        course.delete();

        log.info("[CourseService] 강좌 삭제 완료 - courseId: {}", courseId);
    }
}