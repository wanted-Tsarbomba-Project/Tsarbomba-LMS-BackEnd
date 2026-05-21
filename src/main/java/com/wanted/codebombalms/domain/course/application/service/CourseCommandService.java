package com.wanted.codebombalms.domain.course.application.service;

import com.wanted.codebombalms.domain.course.application.command.CreateCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.PublishCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.UpdateCourseCommand;
import com.wanted.codebombalms.domain.course.application.policy.CoursePublishPolicy;
import com.wanted.codebombalms.domain.course.application.usecase.CourseCommandUseCase;
import com.wanted.codebombalms.domain.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.course.domain.model.CourseStatus;
import com.wanted.codebombalms.domain.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.domain.course.presentation.api.response.CourseDetailResponse;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.infrastructure.logging.aop.LogBusiness;
import com.wanted.codebombalms.global.infrastructure.logging.aop.LogPerformance;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseCommandService implements CourseCommandUseCase {

    private static final Logger log = LoggerFactory.getLogger(CourseCommandService.class);

    private final CourseRepository courseRepository;
    private final CoursePublishPolicy coursePublishPolicy;

    @LogBusiness
    @LogPerformance
    @Override
    public CourseDetailResponse createCourse(CreateCourseCommand command) {

        log.info("[CourseCommandService] 강좌 등록 시작 - title: {}", command.title());

        Course course = Course.create(
                command.instructorId(),
                command.title(),
                command.description(),
                command.thumbnailUrl()
        );

        Course savedCourse = courseRepository.save(course);

        log.info("[CourseCommandService] 강좌 등록 완료 - courseId: {}", savedCourse.getCourseId());

        return CourseDetailResponse.from(savedCourse);
    }

    @Override
    public CourseDetailResponse updateCourse(UpdateCourseCommand command) {

        log.info("[CourseCommandService] 강좌 수정 시작 - courseId: {}", command.courseId());

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(command.courseId())
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        if (command.status() == CourseStatus.ACTIVE && course.getStatus() != CourseStatus.ACTIVE) {
            throw new ValidationException(CourseErrorCode.COURSE_ACTIVE_STATUS_REQUIRES_PUBLISH);
        }

        course.update(
                command.title(),
                command.description(),
                command.thumbnailUrl(),
                command.status()
        );
        Course savedCourse = courseRepository.save(course);

        log.info("[CourseCommandService] 강좌 수정 완료 - courseId: {}", command.courseId());

        return CourseDetailResponse.from(savedCourse);
    }

    @Override
    public CourseDetailResponse publishCourse(PublishCourseCommand command) {

        log.info("[CourseCommandService] 강좌 개설 시작 - courseId: {}", command.courseId());

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(command.courseId())
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        coursePublishPolicy.validate(course);
        course.publish();

        Course savedCourse = courseRepository.save(course);

        log.info("[CourseCommandService] 강좌 개설 완료 - courseId: {}", command.courseId());

        return CourseDetailResponse.from(savedCourse);
    }

    @Override
    public void deleteCourse(Long courseId) {

        log.info("[CourseCommandService] 강좌 삭제 시작 - courseId: {}", courseId);

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        course.delete();
        courseRepository.save(course);

        log.info("[CourseCommandService] 강좌 삭제 완료 - courseId: {}", courseId);
    }
}
