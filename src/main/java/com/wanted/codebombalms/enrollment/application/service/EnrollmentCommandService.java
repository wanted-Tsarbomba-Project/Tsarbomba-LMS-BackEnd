package com.wanted.codebombalms.enrollment.application.service;

import com.wanted.codebombalms.enrollment.application.command.CancelEnrollmentCommand;
import com.wanted.codebombalms.enrollment.application.command.EnrollCourseCommand;
import com.wanted.codebombalms.enrollment.application.policy.EnrollmentEligibilityPolicy;
import com.wanted.codebombalms.enrollment.application.port.CourseCatalogPort;
import com.wanted.codebombalms.enrollment.application.port.CoursePublicationStatus;
import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentCommandUseCase;
import com.wanted.codebombalms.enrollment.domain.exception.EnrollmentErrorCode;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentCommandService implements EnrollmentCommandUseCase {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentCommandService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final CourseCatalogPort courseCatalogPort;
    private final EnrollmentEligibilityPolicy enrollmentEligibilityPolicy;

    @Override
    public Enrollment createEnrollment(EnrollCourseCommand command) {
        log.info("[EnrollmentCommandService] create enrollment - courseId: {}, userId: {}",
                command.courseId(),
                command.userId()
        );

        CoursePublicationStatus course = courseCatalogPort.getPublicationStatus(command.courseId());
        enrollmentEligibilityPolicy.validate(command.userId(), course);

        return enrollmentRepository.findByCourseIdAndUserIdAndStatus(
                        course.courseId(),
                        command.userId(),
                        EnrollmentStatus.CANCELED
                )
                .map(enrollment -> {
                    enrollment.reactivate();
                    return enrollmentRepository.save(enrollment);
                })
                .orElseGet(() -> enrollmentRepository.save(Enrollment.create(
                        command.userId(),
                        course.courseId()
                )));
    }

    @Override
    public void cancelEnrollment(CancelEnrollmentCommand command) {
        log.info("[EnrollmentCommandService] cancel enrollment - userId: {}, enrollmentId: {}",
                command.userId(),
                command.enrollmentId()
        );

        Enrollment enrollment = enrollmentRepository
                .findByEnrollmentIdAndUserIdAndStatus(command.enrollmentId(), command.userId(), EnrollmentStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND));

        enrollment.cancel();
        enrollmentRepository.save(enrollment);
    }
}
