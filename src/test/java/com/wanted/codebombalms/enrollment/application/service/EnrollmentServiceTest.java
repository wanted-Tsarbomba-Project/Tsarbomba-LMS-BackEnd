package com.wanted.codebombalms.enrollment.application.service;

import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.enrollment.application.command.CancelEnrollmentCommand;
import com.wanted.codebombalms.enrollment.application.command.EnrollCourseCommand;
import com.wanted.codebombalms.enrollment.application.policy.EnrollmentEligibilityPolicy;
import com.wanted.codebombalms.enrollment.application.port.CourseCatalogPort;
import com.wanted.codebombalms.enrollment.application.port.CoursePublicationStatus;
import com.wanted.codebombalms.enrollment.application.service.EnrollmentCommandService;
import com.wanted.codebombalms.enrollment.application.service.EnrollmentQueryService;
import com.wanted.codebombalms.enrollment.domain.exception.EnrollmentErrorCode;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Enrollment application service unit test")
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseCatalogPort courseCatalogPort;

    @Mock
    private EnrollmentEligibilityPolicy enrollmentEligibilityPolicy;

    @InjectMocks
    private EnrollmentCommandService enrollmentCommandService;

    @InjectMocks
    private EnrollmentQueryService enrollmentQueryService;

    @Test
    void createEnrollment_returnsEnrollment() {
        Long courseId = 1L;
        Long userId = 10L;
        CoursePublicationStatus course = createCourseStatus(courseId);
        Enrollment savedEnrollment = createEnrollment(1L, userId, courseId, EnrollmentStatus.ACTIVE);

        given(courseCatalogPort.getPublicationStatus(courseId)).willReturn(course);
        given(enrollmentRepository.save(any(Enrollment.class))).willReturn(savedEnrollment);

        Enrollment result = enrollmentCommandService.createEnrollment(new EnrollCourseCommand(userId, courseId));

        assertEquals(1L, result.getEnrollmentId());
        assertEquals(courseId, result.getCourseId());
        assertEquals(userId, result.getUserId());
        assertEquals(EnrollmentStatus.ACTIVE, result.getStatus());
        verify(enrollmentEligibilityPolicy).validate(userId, course);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void createEnrollment_throwsNotFound_whenCourseMissing() {
        Long courseId = 999L;
        given(courseCatalogPort.getPublicationStatus(courseId))
                .willThrow(new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> enrollmentCommandService.createEnrollment(new EnrollCourseCommand(10L, courseId))
        );

        assertEquals(CourseErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void findMyCourses_returnsActiveEnrollments() {
        Long userId = 10L;
        Enrollment enrollment = createEnrollment(1L, userId, 1L, EnrollmentStatus.ACTIVE);

        given(enrollmentRepository.findByUserIdAndStatus(userId, EnrollmentStatus.ACTIVE))
                .willReturn(List.of(enrollment));

        List<Enrollment> results = enrollmentQueryService.findMyCourses(userId);

        assertEquals(1, results.size());
        assertEquals(userId, results.get(0).getUserId());
        assertEquals(1L, results.get(0).getCourseId());
    }

    @Test
    void cancelEnrollment_cancelsEnrollment() {
        Long userId = 10L;
        Long enrollmentId = 1L;
        Enrollment enrollment = createEnrollment(enrollmentId, userId, 1L, EnrollmentStatus.ACTIVE);

        given(enrollmentRepository.findByEnrollmentIdAndUserIdAndStatus(
                enrollmentId,
                userId,
                EnrollmentStatus.ACTIVE
        )).willReturn(Optional.of(enrollment));
        given(enrollmentRepository.save(enrollment)).willReturn(enrollment);

        enrollmentCommandService.cancelEnrollment(new CancelEnrollmentCommand(userId, enrollmentId));

        assertEquals(EnrollmentStatus.CANCELED, enrollment.getStatus());
        assertNotNull(enrollment.getCanceledAt());
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void cancelEnrollment_throwsNotFound_whenMissing() {
        Long userId = 10L;
        Long enrollmentId = 999L;

        given(enrollmentRepository.findByEnrollmentIdAndUserIdAndStatus(
                enrollmentId,
                userId,
                EnrollmentStatus.ACTIVE
        )).willReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> enrollmentCommandService.cancelEnrollment(new CancelEnrollmentCommand(userId, enrollmentId))
        );

        assertEquals(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND, exception.getErrorCode());
    }

    private CoursePublicationStatus createCourseStatus(Long courseId) {
        return new CoursePublicationStatus(courseId, 1L, "Java", "description", "course.png", true);
    }

    private Enrollment createEnrollment(
            Long enrollmentId,
            Long userId,
            Long courseId,
            EnrollmentStatus status
    ) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(enrollmentId);
        enrollment.setUserId(userId);
        enrollment.setCourseId(courseId);
        enrollment.setStatus(status);
        enrollment.setEnrolledAt(LocalDateTime.now());
        return enrollment;
    }
}
