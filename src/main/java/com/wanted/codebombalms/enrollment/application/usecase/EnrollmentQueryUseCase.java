package com.wanted.codebombalms.enrollment.application.usecase;

import com.wanted.codebombalms.enrollment.domain.model.Enrollment;

import java.util.List;

public interface EnrollmentQueryUseCase {

    List<Enrollment> findMyCourses(Long userId);
}
