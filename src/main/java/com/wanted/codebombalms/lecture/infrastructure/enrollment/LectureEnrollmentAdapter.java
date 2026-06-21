package com.wanted.codebombalms.lecture.infrastructure.enrollment;

import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentQueryUseCase;
import com.wanted.codebombalms.lecture.application.port.LectureEnrollmentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureEnrollmentAdapter implements LectureEnrollmentPort {

    private final EnrollmentQueryUseCase enrollmentQueryUseCase;

    @Override
    public boolean isActiveStudentOfCourse(Long courseId, Long userId) {
        return enrollmentQueryUseCase.isActiveStudentOfCourse(courseId, userId);
    }
}
