package com.wanted.codebombalms.course.infrastructure.enrollment;

import com.wanted.codebombalms.course.application.port.CourseEnrollmentPort;
import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseEnrollmentAdapter implements CourseEnrollmentPort {

    private final EnrollmentQueryUseCase enrollmentQueryUseCase;

    @Override
    public boolean isActiveStudentOfCourse(Long courseId, Long userId) {
        return enrollmentQueryUseCase.isActiveStudentOfCourse(courseId, userId);
    }
}
