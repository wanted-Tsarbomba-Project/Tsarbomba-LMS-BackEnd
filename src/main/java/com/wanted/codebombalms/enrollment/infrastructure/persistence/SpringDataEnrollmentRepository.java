package com.wanted.codebombalms.enrollment.infrastructure.persistence;

import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataEnrollmentRepository extends JpaRepository<EnrollmentJpaEntity, Long> {

    boolean existsByCourse_CourseIdAndUserIdAndStatus(
            Long courseId,
            Long userId,
            EnrollmentStatus status
    );

    boolean existsByCourse_CourseIdAndUserId(Long courseId, Long userId);

    List<EnrollmentJpaEntity> findByUserIdAndStatus(Long userId, EnrollmentStatus status);

    List<EnrollmentJpaEntity> findByCourse_CourseIdAndStatus(Long courseId, EnrollmentStatus status);

    List<EnrollmentJpaEntity> findByStatus(EnrollmentStatus status);

    Optional<EnrollmentJpaEntity> findByEnrollmentIdAndStatus(Long enrollmentId, EnrollmentStatus status);

    Optional<EnrollmentJpaEntity> findByCourse_CourseIdAndUserIdAndStatus(
            Long courseId,
            Long userId,
            EnrollmentStatus status
    );

    Optional<EnrollmentJpaEntity> findByEnrollmentIdAndUserIdAndStatus(
            Long enrollmentId,
            Long userId,
            EnrollmentStatus status
    );
}
