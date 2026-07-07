package com.wanted.codebombalms.enrollment.application.policy;

import com.wanted.codebombalms.enrollment.application.policy.EnrollmentEligibilityPolicy;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentEligibilityPolicy unit test")
class EnrollmentEligibilityPolicyTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserCatalogPort userCatalogPort;

    @InjectMocks
    private EnrollmentEligibilityPolicy enrollmentEligibilityPolicy;

    @Test
    void validate_passes_whenUserIsActiveStudentAndCourseIsPublished() {
        CoursePublicationStatus course = createCourseStatus(true);
        given(userCatalogPort.getEnrollmentEligibility(10L))
                .willReturn(new UserEnrollmentEligibility(true, false));
        given(enrollmentRepository.existsByCourseIdAndUserIdAndStatus(1L, 10L, EnrollmentStatus.ACTIVE))
                .willReturn(false);

        enrollmentEligibilityPolicy.validate(10L, course);
    }

    @Test
    void validate_throwsValidation_whenUserIsNotActiveStudent() {
        CoursePublicationStatus course = createCourseStatus(true);
        given(userCatalogPort.getEnrollmentEligibility(10L))
                .willReturn(new UserEnrollmentEligibility(false, false));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> enrollmentEligibilityPolicy.validate(10L, course)
        );

        assertEquals(EnrollmentErrorCode.ENROLLMENT_STUDENT_REQUIRED, exception.getErrorCode());
    }

    @Test
    void validate_throwsForbidden_whenUserIsLocked() {
        CoursePublicationStatus course = createCourseStatus(true);
        given(userCatalogPort.getEnrollmentEligibility(10L))
                .willReturn(new UserEnrollmentEligibility(false, true));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> enrollmentEligibilityPolicy.validate(10L, course)
        );

        assertEquals(UserErrorCode.USER_ACCOUNT_LOCKED, exception.getErrorCode());
    }

    @Test
    void validate_throwsValidation_whenCourseIsNotPublished() {
        CoursePublicationStatus course = createCourseStatus(false);
        given(userCatalogPort.getEnrollmentEligibility(10L))
                .willReturn(new UserEnrollmentEligibility(true, false));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> enrollmentEligibilityPolicy.validate(10L, course)
        );

        assertEquals(EnrollmentErrorCode.COURSE_NOT_ENROLLABLE, exception.getErrorCode());
    }

    @Test
    void validate_throwsConflict_whenActiveEnrollmentAlreadyExists() {
        CoursePublicationStatus course = createCourseStatus(true);
        given(userCatalogPort.getEnrollmentEligibility(10L))
                .willReturn(new UserEnrollmentEligibility(true, false));
        given(enrollmentRepository.existsByCourseIdAndUserIdAndStatus(1L, 10L, EnrollmentStatus.ACTIVE))
                .willReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> enrollmentEligibilityPolicy.validate(10L, course)
        );

        assertEquals(EnrollmentErrorCode.DUPLICATE_ENROLLMENT, exception.getErrorCode());
    }

    private CoursePublicationStatus createCourseStatus(boolean published) {
        return new CoursePublicationStatus(1L, 2L, "Java", "description", "java.png", published);
    }
}
