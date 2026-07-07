package com.wanted.codebombalms.enrollment.application.policy;

import com.wanted.codebombalms.enrollment.application.port.CoursePublicationStatus;
import com.wanted.codebombalms.enrollment.application.port.UserCatalogPort;
import com.wanted.codebombalms.enrollment.application.port.UserCatalogPort.UserEnrollmentEligibility;
import com.wanted.codebombalms.enrollment.domain.exception.EnrollmentErrorCode;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentEligibilityPolicy {

    private final EnrollmentRepository enrollmentRepository;
    private final UserCatalogPort userCatalogPort;

    public void validate(Long userId, CoursePublicationStatus course) {
        UserEnrollmentEligibility eligibility = userCatalogPort.getEnrollmentEligibility(userId);
        if (eligibility.locked()) {
            throw new ForbiddenException(UserErrorCode.USER_ACCOUNT_LOCKED);
        }
        if (!eligibility.activeStudent()) {
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
