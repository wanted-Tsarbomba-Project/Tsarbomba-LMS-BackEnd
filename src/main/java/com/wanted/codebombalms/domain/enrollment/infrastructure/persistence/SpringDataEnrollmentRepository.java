package com.wanted.codebombalms.domain.enrollment.infrastructure.persistence;

import com.wanted.codebombalms.domain.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataEnrollmentRepository extends JpaRepository<EnrollmentJpaEntity, Long> {

    boolean existsByCourse_CourseIdAndStudentAndStatus(
            Long courseId,
            User student,
            EnrollmentStatus status
    );

    List<EnrollmentJpaEntity> findByStudentAndStatus(
            User student,
            EnrollmentStatus status
    );

    Optional<EnrollmentJpaEntity> findByEnrollmentIdAndStatus(
            Long enrollmentId,
            EnrollmentStatus status
    );

    Optional<EnrollmentJpaEntity> findByEnrollmentIdAndStudentAndStatus(
            Long enrollmentId,
            User student,
            EnrollmentStatus status
    );
}
