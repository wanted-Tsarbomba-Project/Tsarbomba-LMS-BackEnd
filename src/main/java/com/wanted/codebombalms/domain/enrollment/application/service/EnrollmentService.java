package com.wanted.codebombalms.domain.enrollment.application.service;

import com.wanted.codebombalms.domain.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.course.domain.repository.CourseRepository;
import com.wanted.codebombalms.domain.enrollment.domain.exception.EnrollmentErrorCode;
import com.wanted.codebombalms.domain.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.domain.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.domain.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.codebombalms.domain.enrollment.presentation.api.request.EnrollmentCreateRequest;
import com.wanted.codebombalms.domain.enrollment.presentation.api.response.EnrollmentResponse;
import com.wanted.codebombalms.domain.enrollment.presentation.api.response.MyCourseResponse;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public EnrollmentResponse createEnrollment(Long courseId, EnrollmentCreateRequest request) {
        Long studentId = request.getStudentId();
        log.info("[EnrollmentService] create enrollment - courseId: {}, studentId: {}", courseId, studentId);

        Course course = courseRepository.findByCourseIdAndDeletedAtIsNull(courseId)
                .orElseThrow(() -> new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND));

        boolean alreadyEnrolled = enrollmentRepository.existsByCourseAndStudentIdAndStatus(
                course,
                studentId,
                EnrollmentStatus.ACTIVE
        );

        if (alreadyEnrolled) {
            throw new ConflictException(EnrollmentErrorCode.DUPLICATE_ENROLLMENT);
        }

        Enrollment enrollment = Enrollment.create(course, studentId);
        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }

    public List<MyCourseResponse> findMyCourses(Long studentId) {
        log.info("[EnrollmentService] find my courses - studentId: {}", studentId);

        return enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE)
                .stream()
                .map(MyCourseResponse::from)
                .toList();
    }

    @Transactional
    public void cancelEnrollment(Long studentId, Long enrollmentId) {
        log.info("[EnrollmentService] cancel enrollment - studentId: {}, enrollmentId: {}", studentId, enrollmentId);

        Enrollment enrollment = enrollmentRepository
                .findByEnrollmentIdAndStudentIdAndStatus(enrollmentId, studentId, EnrollmentStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND));

        enrollment.cancel();
        enrollmentRepository.save(enrollment);
    }
}
