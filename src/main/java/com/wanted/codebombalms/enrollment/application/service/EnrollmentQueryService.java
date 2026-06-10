package com.wanted.codebombalms.enrollment.application.service;

import com.wanted.codebombalms.enrollment.application.port.CourseCatalogPort;
import com.wanted.codebombalms.enrollment.application.query.MyCourseResult;
import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentQueryUseCase;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.enrollment.domain.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentQueryService implements EnrollmentQueryUseCase {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentQueryService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final CourseCatalogPort courseCatalogPort;

    @Override
    public List<MyCourseResult> findMyCourses(Long userId) {
        log.info("[EnrollmentQueryService] find my courses - userId: {}", userId);

        return enrollmentRepository.findByUserIdAndStatus(userId, EnrollmentStatus.ACTIVE)
                .stream()
                .map(enrollment -> MyCourseResult.from(
                        enrollment,
                        courseCatalogPort.getPublicationStatus(enrollment.getCourseId())
                ))
                .toList();
    }

    @Override
    public List<Enrollment> findAllActiveEnrollments() {
        log.info("[EnrollmentQueryService] find all active enrollments");

        return enrollmentRepository.findByStatus(EnrollmentStatus.ACTIVE);
    }

    @Override
    public List<Long> findActiveStudentIdsByCourse(Long courseId) {
        log.info("[EnrollmentQueryService] find active student ids - courseId: {}", courseId);

        return enrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE)
                .stream()
                .map(Enrollment::getUserId)
                .toList();
    }
}
