package com.wanted.codebombalms.learning.infrastructure.enrollment;

import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentQueryUseCase;
import com.wanted.codebombalms.learning.application.port.LearningEnrollmentPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningEnrollmentAdapter implements LearningEnrollmentPort {

    private final EnrollmentQueryUseCase enrollmentQueryUseCase;

    @Override
    public List<Long> findActiveStudentIdsByCourse(Long courseId) {
        return enrollmentQueryUseCase.findActiveStudentIdsByCourse(courseId);
    }
}
