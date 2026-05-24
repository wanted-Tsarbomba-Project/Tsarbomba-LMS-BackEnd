package com.wanted.codebombalms.domain.enrollment.application.policy;

import com.wanted.codebombalms.enrollment.application.policy.EnrollmentEligibilityPolicy;
import com.wanted.codebombalms.enrollment.application.port.CoursePublicationStatus;
import com.wanted.codebombalms.enrollment.application.port.UserCatalogPort;
import com.wanted.codebombalms.enrollment.domain.exception.EnrollmentErrorCode;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
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
        given(userCatalogPort.isActiveStudent(10L)).willReturn(true);
        given(enrollmentRepository.existsByCourseIdAndUserIdAndStatus(1L, 10L, EnrollmentStatus.ACTIVE))
                .willReturn(false);

        enrollmentEligibilityPolicy.validate(10L, course);
    }

    @Test
    void validate_throwsValidation_whenUserIsNotActiveStudent() {
        CoursePublicationStatus course = createCourseStatus(true);
        given(userCatalogPort.isActiveStudent(10L)).willReturn(false);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> enrollmentEligibilityPolicy.validate(10L, course)
        );

        assertEquals(EnrollmentErrorCode.ENROLLMENT_STUDENT_REQUIRED, exception.getErrorCode());
    }

    @Test
    void validate_throwsValidation_whenCourseIsNotPublished() {
        CoursePublicationStatus course = createCourseStatus(false);
        given(userCatalogPort.isActiveStudent(10L)).willReturn(true);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> enrollmentEligibilityPolicy.validate(10L, course)
        );

        assertEquals(EnrollmentErrorCode.COURSE_NOT_ENROLLABLE, exception.getErrorCode());
    }

    @Test
    void validate_throwsConflict_whenActiveEnrollmentAlreadyExists() {
        CoursePublicationStatus course = createCourseStatus(true);
        given(userCatalogPort.isActiveStudent(10L)).willReturn(true);
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
