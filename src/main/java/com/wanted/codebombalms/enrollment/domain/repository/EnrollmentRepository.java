package com.wanted.codebombalms.enrollment.domain.repository;

import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository {

    Enrollment save(Enrollment enrollment);

    boolean existsByCourseIdAndUserIdAndStatus(Long courseId, Long userId, EnrollmentStatus status);

    boolean existsByCourseIdAndUserId(Long courseId, Long userId);

    List<Enrollment> findByUserIdAndStatus(Long userId, EnrollmentStatus status);

    Optional<Enrollment> findByEnrollmentIdAndStatus(Long enrollmentId, EnrollmentStatus status);

    List<Enrollment> findByStatus(EnrollmentStatus status);

    Optional<Enrollment> findByEnrollmentIdAndUserIdAndStatus(
            Long enrollmentId,
            Long userId,
            EnrollmentStatus status
    );
}
