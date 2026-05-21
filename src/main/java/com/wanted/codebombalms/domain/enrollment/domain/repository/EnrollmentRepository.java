package com.wanted.codebombalms.domain.enrollment.domain.repository;

import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.domain.enrollment.domain.model.EnrollmentStatus;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository {

    Enrollment save(Enrollment enrollment);

    boolean existsByCourseAndStudentIdAndStatus(Course course, Long studentId, EnrollmentStatus status);

    List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    Optional<Enrollment> findByEnrollmentIdAndStatus(Long enrollmentId, EnrollmentStatus status);

    Optional<Enrollment> findByEnrollmentIdAndStudentIdAndStatus(
            Long enrollmentId,
            Long studentId,
            EnrollmentStatus status
    );
}
