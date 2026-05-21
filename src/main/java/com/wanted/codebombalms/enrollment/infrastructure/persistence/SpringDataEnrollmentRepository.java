package com.wanted.codebombalms.enrollment.infrastructure.persistence;

import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataEnrollmentRepository extends JpaRepository<EnrollmentJpaEntity, Long> {

    boolean existsByCourse_CourseIdAndStudentIdAndStatus(
            Long courseId,
            Long studentId,
            EnrollmentStatus status
    );

    List<EnrollmentJpaEntity> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    Optional<EnrollmentJpaEntity> findByEnrollmentIdAndStatus(Long enrollmentId, EnrollmentStatus status);

    Optional<EnrollmentJpaEntity> findByEnrollmentIdAndStudentIdAndStatus(
            Long enrollmentId,
            Long studentId,
            EnrollmentStatus status
    );
}
