package com.wanted.codebombalms.domain.enrollment.domain.repository;

import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.domain.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository {

    Enrollment save(Enrollment enrollment);

    boolean existsByCourseAndStudentAndStatus(
            Course course,
            User student,
            EnrollmentStatus status
    );

    List<Enrollment> findByStudentAndStatus(
            User student,
            EnrollmentStatus status
    );

    Optional<Enrollment> findByEnrollmentIdAndStatus(
            Long enrollmentId,
            EnrollmentStatus status
    );

    Optional<Enrollment> findByEnrollmentIdAndStudentAndStatus(
            Long enrollmentId,
            User student,
            EnrollmentStatus status
    );
}
