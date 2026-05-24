package com.wanted.codebombalms.course.application.service;

import com.wanted.codebombalms.course.application.command.CreateCourseCommand;
import com.wanted.codebombalms.course.application.command.PublishCourseCommand;
import com.wanted.codebombalms.course.application.command.UpdateCourseCommand;
import com.wanted.codebombalms.course.application.policy.CourseAuthorPolicy;
import com.wanted.codebombalms.course.application.policy.CoursePublishPolicy;
import com.wanted.codebombalms.course.application.usecase.CourseCommandUseCase;
import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
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
    private final CourseAuthorPolicy courseAuthorPolicy;
    private final CoursePublishPolicy coursePublishPolicy;

    @LogBusiness
    @LogPerformance
    @Override
    public Course createCourse(CreateCourseCommand command) {
        log.info("[CourseCommandService] create course - title: {}", command.title());

        courseAuthorPolicy.validateOperator(command.instructorId());

        Course course = Course.create(
                command.instructorId(),
                command.title(),
                command.description(),
                command.thumbnailUrl()
        );

        Course savedCourse = courseRepository.save(course);
        log.info("[CourseCommandService] created course - courseId: {}", savedCourse.getCourseId());

        return savedCourse;
    }

    @Override
    public Course updateCourse(UpdateCourseCommand command) {
        log.info("[CourseCommandService] update course - courseId: {}", command.courseId());

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(command.courseId())
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        validateStatusChange(command.status(), course.getStatus());

        course.update(
                command.title(),
                command.description(),
                command.thumbnailUrl(),
                command.status()
        );

        Course savedCourse = courseRepository.save(course);
        log.info("[CourseCommandService] updated course - courseId: {}", command.courseId());

        return savedCourse;
    }

    @Override
    public Course publishCourse(PublishCourseCommand command) {
        log.info("[CourseCommandService] publish course - courseId: {}", command.courseId());

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(command.courseId())
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        coursePublishPolicy.validate(course);
        course.publish();

        Course savedCourse = courseRepository.save(course);
        log.info("[CourseCommandService] published course - courseId: {}", command.courseId());

        return savedCourse;
    }

    @Override
    public void deleteCourse(Long courseId) {
        log.info("[CourseCommandService] delete course - courseId: {}", courseId);

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        course.delete();
        courseRepository.save(course);

        log.info("[CourseCommandService] deleted course - courseId: {}", courseId);
    }

    private void validateStatusChange(CourseStatus requestedStatus, CourseStatus currentStatus) {
        if (requestedStatus == null) {
            return;
        }
        if (requestedStatus == CourseStatus.ACTIVE && currentStatus != CourseStatus.ACTIVE) {
            throw new ValidationException(CourseErrorCode.COURSE_ACTIVE_STATUS_REQUIRES_PUBLISH);
        }
        if (requestedStatus == CourseStatus.DELETED) {
            throw new ValidationException(CourseErrorCode.COURSE_DELETE_STATUS_REQUIRES_DELETE);
        }
    }
}
