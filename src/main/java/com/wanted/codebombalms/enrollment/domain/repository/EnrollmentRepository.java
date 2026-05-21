package com.wanted.codebombalms.enrollment.domain.repository;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;

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
