package com.wanted.codebombalms.learning.infrastructure.enrollment;

import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.enrollment.infrastructure.persistence.EnrollmentJpaEntity;
import com.wanted.codebombalms.enrollment.infrastructure.persistence.SpringDataEnrollmentRepository;
import com.wanted.codebombalms.learning.application.port.LearningEnrollmentPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningEnrollmentAdapter implements LearningEnrollmentPort {

    private final SpringDataEnrollmentRepository enrollmentRepository;

    @Override
    public List<Long> findActiveStudentIdsByCourse(Long courseId) {
        return enrollmentRepository.findByCourse_CourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE)
                .stream()
                .map(EnrollmentJpaEntity::getUserId)
                .toList();
    }
}
