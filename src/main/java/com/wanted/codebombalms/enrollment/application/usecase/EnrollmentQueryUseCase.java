package com.wanted.codebombalms.enrollment.application.usecase;

import com.wanted.codebombalms.enrollment.application.query.MyCourseResult;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;

import java.util.List;

public interface EnrollmentQueryUseCase {

    List<MyCourseResult> findMyCourses(Long userId);

    List<Enrollment> findAllActiveEnrollments();

    List<Long> findActiveStudentIdsByCourse(Long courseId);

    boolean isActiveStudentOfCourse(Long courseId, Long userId);
}
