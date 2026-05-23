package com.wanted.codebombalms.enrollment.application.policy;

import com.wanted.codebombalms.enrollment.application.port.CoursePublicationStatus;
import com.wanted.codebombalms.enrollment.application.port.UserCatalogPort;
import com.wanted.codebombalms.enrollment.domain.exception.EnrollmentErrorCode;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentEligibilityPolicy {

    private final EnrollmentRepository enrollmentRepository;
    private final UserCatalogPort userCatalogPort;

    public void validate(Long userId, CoursePublicationStatus course) {
        if (!userCatalogPort.isActiveStudent(userId)) {
            throw new ValidationException(EnrollmentErrorCode.ENROLLMENT_STUDENT_REQUIRED);
        }

        if (!course.published()) {
            throw new ValidationException(EnrollmentErrorCode.COURSE_NOT_ENROLLABLE);
        }

        if (enrollmentRepository.existsByCourseIdAndUserIdAndStatus(
                course.courseId(),
                userId,
                EnrollmentStatus.ACTIVE
        )) {
            throw new ConflictException(EnrollmentErrorCode.DUPLICATE_ENROLLMENT);
        }
    }
}
