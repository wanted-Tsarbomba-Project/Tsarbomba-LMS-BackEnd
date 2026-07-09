package com.wanted.codebombalms.enrollment.application.service;

import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.enrollment.application.port.CourseCatalogPort;
import com.wanted.codebombalms.enrollment.application.port.CoursePublicationStatus;
import com.wanted.codebombalms.enrollment.application.port.EnrollmentLearningProgressPort;
import com.wanted.codebombalms.enrollment.application.port.EnrollmentLearningProgressPort.EnrollmentLearningProgress;
import com.wanted.codebombalms.enrollment.application.port.EnrollmentLearningProgressPort.EnrollmentLearningProgressKey;
import com.wanted.codebombalms.enrollment.application.query.MyCourseResult;
import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentQueryUseCase;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentQueryService implements EnrollmentQueryUseCase {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentQueryService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final CourseCatalogPort courseCatalogPort;
    private final EnrollmentLearningProgressPort enrollmentLearningProgressPort;

    @Override
    public List<MyCourseResult> findMyCourses(Long userId) {
        log.info("[EnrollmentQueryService] find my courses - userId: {}", userId);

        List<Enrollment> enrollments = enrollmentRepository.findByUserIdAndStatus(userId, EnrollmentStatus.ACTIVE);
        List<MyCourseEnrollment> visibleEnrollments = enrollments.stream()
                .map(enrollment -> toVisibleEnrollment(userId, enrollment))
                .flatMap(Optional::stream)
                .toList();

        if (visibleEnrollments.isEmpty()) {
            return List.of();
        }

        Map<EnrollmentLearningProgressKey, EnrollmentLearningProgress> progresses =
                enrollmentLearningProgressPort.findProgresses(visibleEnrollments.stream()
                        .map(visibleEnrollment -> new EnrollmentLearningProgressKey(
                                userId,
                                visibleEnrollment.enrollment().getCourseId()
                        ))
                        .toList());

        return visibleEnrollments.stream()
                .map(visibleEnrollment -> toMyCourseResult(userId, visibleEnrollment, progresses))
                .toList();
    }

    private Optional<MyCourseEnrollment> toVisibleEnrollment(
            Long userId,
            Enrollment enrollment
    ) {
        try {
            return Optional.of(new MyCourseEnrollment(
                    enrollment,
                    courseCatalogPort.getPublicationStatus(enrollment.getCourseId())
            ));
        } catch (NotFoundException e) {
            if (e.getErrorCode() != CourseErrorCode.COURSE_NOT_FOUND) {
                throw e;
            }

            log.info(
                    "[EnrollmentQueryService] skip deleted course enrollment - userId: {}, enrollmentId: {}, courseId: {}",
                    userId,
                    enrollment.getEnrollmentId(),
                    enrollment.getCourseId()
            );
            return Optional.empty();
        }
    }

    private MyCourseResult toMyCourseResult(
            Long userId,
            MyCourseEnrollment visibleEnrollment,
            Map<EnrollmentLearningProgressKey, EnrollmentLearningProgress> progresses
    ) {
        Enrollment enrollment = visibleEnrollment.enrollment();
        EnrollmentLearningProgressKey progressKey = new EnrollmentLearningProgressKey(userId, enrollment.getCourseId());

        return MyCourseResult.from(
                enrollment,
                visibleEnrollment.course(),
                progresses.getOrDefault(progressKey, EnrollmentLearningProgress.of(0, 0, 0, 0))
        );
    }

    private record MyCourseEnrollment(
            Enrollment enrollment,
            CoursePublicationStatus course
    ) {
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

    @Override
    public List<Long> findActiveStudentIdsByCourse(Long courseId, int page, int size) {
        log.info("[EnrollmentQueryService] find active student ids page - courseId: {}, page: {}, size: {}",
                courseId, page, size);

        return enrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE, page, size)
                .stream()
                .map(Enrollment::getUserId)
                .toList();
    }

    @Override
    public long countActiveStudentsByCourse(Long courseId) {
        return enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
    }

    @Override
    public boolean isActiveStudentOfCourse(Long courseId, Long userId) {
        log.debug("[EnrollmentQueryService] check active enrollment - courseId: {}, userId: {}", courseId, userId);

        return enrollmentRepository.existsByCourseIdAndUserIdAndStatus(courseId, userId, EnrollmentStatus.ACTIVE);
    }
}
