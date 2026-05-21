package com.wanted.codebombalms.domain.enrollment.application.service;

import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.enrollment.application.service.EnrollmentService;
import com.wanted.codebombalms.enrollment.domain.exception.EnrollmentErrorCode;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.codebombalms.enrollment.presentation.api.request.EnrollmentCreateRequest;
import com.wanted.codebombalms.enrollment.presentation.api.response.EnrollmentResponse;
import com.wanted.codebombalms.enrollment.presentation.api.response.MyCourseResponse;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
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
@DisplayName("EnrollmentService unit test")
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    void createEnrollment_returnsResponse() {
        Long courseId = 1L;
        Long studentId = 10L;
        Course course = createCourse(courseId, "Java");
        Enrollment savedEnrollment = createEnrollment(1L, course, studentId, EnrollmentStatus.ACTIVE);

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));
        given(enrollmentRepository.existsByCourseAndStudentIdAndStatus(course, studentId, EnrollmentStatus.ACTIVE))
                .willReturn(false);
        given(enrollmentRepository.save(any(Enrollment.class))).willReturn(savedEnrollment);

        EnrollmentResponse response = enrollmentService.createEnrollment(courseId, new EnrollmentCreateRequest(studentId));

        assertEquals(1L, response.getEnrollmentId());
        assertEquals(courseId, response.getCourseId());
        assertEquals(studentId, response.getStudentId());
        assertEquals(EnrollmentStatus.ACTIVE, response.getStatus());
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void createEnrollment_throwsNotFound_whenCourseMissing() {
        Long courseId = 999L;
        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> enrollmentService.createEnrollment(courseId, new EnrollmentCreateRequest(10L))
        );

        assertEquals(CourseErrorCode.COURSE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void createEnrollment_throwsConflict_whenAlreadyEnrolled() {
        Long courseId = 1L;
        Long studentId = 10L;
        Course course = createCourse(courseId, "Java");

        given(courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)).willReturn(Optional.of(course));
        given(enrollmentRepository.existsByCourseAndStudentIdAndStatus(course, studentId, EnrollmentStatus.ACTIVE))
                .willReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> enrollmentService.createEnrollment(courseId, new EnrollmentCreateRequest(studentId))
        );

        assertEquals(EnrollmentErrorCode.DUPLICATE_ENROLLMENT, exception.getErrorCode());
    }

    @Test
    void findMyCourses_returnsActiveEnrollments() {
        Long studentId = 10L;
        Course course = createCourse(1L, "Java");
        Enrollment enrollment = createEnrollment(1L, course, studentId, EnrollmentStatus.ACTIVE);

        given(enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE))
                .willReturn(List.of(enrollment));

        List<MyCourseResponse> responses = enrollmentService.findMyCourses(studentId);

        assertEquals(1, responses.size());
        assertEquals(course.getCourseId(), responses.get(0).getCourseId());
        assertEquals(course.getTitle(), responses.get(0).getCourseTitle());
    }

    @Test
    void cancelEnrollment_cancelsEnrollment() {
        Long studentId = 10L;
        Long enrollmentId = 1L;
        Enrollment enrollment = createEnrollment(enrollmentId, createCourse(1L, "Java"), studentId, EnrollmentStatus.ACTIVE);

        given(enrollmentRepository.findByEnrollmentIdAndStudentIdAndStatus(
                enrollmentId,
                studentId,
                EnrollmentStatus.ACTIVE
        )).willReturn(Optional.of(enrollment));
        given(enrollmentRepository.save(enrollment)).willReturn(enrollment);

        enrollmentService.cancelEnrollment(studentId, enrollmentId);

        assertEquals(EnrollmentStatus.CANCELED, enrollment.getStatus());
        assertNotNull(enrollment.getCanceledAt());
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void cancelEnrollment_throwsNotFound_whenMissing() {
        Long studentId = 10L;
        Long enrollmentId = 999L;

        given(enrollmentRepository.findByEnrollmentIdAndStudentIdAndStatus(
                enrollmentId,
                studentId,
                EnrollmentStatus.ACTIVE
        )).willReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> enrollmentService.cancelEnrollment(studentId, enrollmentId)
        );

        assertEquals(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND, exception.getErrorCode());
    }

    private Course createCourse(Long courseId, String title) {
        Course course = new Course();
        course.setCourseId(courseId);
        course.setInstructorId(1L);
        course.setTitle(title);
        course.setDescription(title + " description");
        course.setThumbnailUrl("course.png");
        course.setCreatedAt(LocalDateTime.now());
        return course;
    }

    private Enrollment createEnrollment(
            Long enrollmentId,
            Course course,
            Long studentId,
            EnrollmentStatus status
    ) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(enrollmentId);
        enrollment.setCourse(course);
        enrollment.setStudentId(studentId);
        enrollment.setStatus(status);
        enrollment.setEnrolledAt(LocalDateTime.now());
        return enrollment;
    }
}
