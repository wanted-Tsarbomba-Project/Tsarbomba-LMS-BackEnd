package com.wanted.codebombalms.enrollment.infrastructure.persistence;

import com.wanted.codebombalms.course.infrastructure.persistence.CourseJpaEntity;
import com.wanted.codebombalms.course.infrastructure.persistence.SpringDataCourseRepository;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.enrollment.domain.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EnrollmentRepositoryAdapter implements EnrollmentRepository {

    private final SpringDataEnrollmentRepository springDataEnrollmentRepository;
    private final SpringDataCourseRepository springDataCourseRepository;

    @Override
    public Enrollment save(Enrollment enrollment) {
        CourseJpaEntity courseEntity = springDataCourseRepository.findById(enrollment.getCourseId())
                .orElseThrow();

        EnrollmentJpaEntity entity = enrollment.getEnrollmentId() == null
                ? EnrollmentJpaEntity.from(enrollment, courseEntity)
                : springDataEnrollmentRepository.findById(enrollment.getEnrollmentId())
                .map(found -> {
                    found.apply(enrollment, courseEntity);
                    return found;
                })
                .orElseGet(() -> EnrollmentJpaEntity.from(enrollment, courseEntity));

        return springDataEnrollmentRepository.save(entity).toDomain();
    }

    @Override
    public boolean existsByCourseIdAndUserIdAndStatus(Long courseId, Long userId, EnrollmentStatus status) {
        return springDataEnrollmentRepository.existsByCourse_CourseIdAndUserIdAndStatus(courseId, userId, status);
    }

    @Override
    public boolean existsByCourseIdAndUserId(Long courseId, Long userId) {
        return springDataEnrollmentRepository.existsByCourse_CourseIdAndUserId(courseId, userId);
    }

    @Override
    public List<Enrollment> findByUserIdAndStatus(Long userId, EnrollmentStatus status) {
        return springDataEnrollmentRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(EnrollmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Enrollment> findByEnrollmentIdAndStatus(Long enrollmentId, EnrollmentStatus status) {
        return springDataEnrollmentRepository.findByEnrollmentIdAndStatus(enrollmentId, status)
                .map(EnrollmentJpaEntity::toDomain);
    }

    @Override
    public Optional<Enrollment> findByCourseIdAndUserIdAndStatus(
            Long courseId,
            Long userId,
            EnrollmentStatus status
    ) {
        return springDataEnrollmentRepository.findByCourse_CourseIdAndUserIdAndStatus(courseId, userId, status)
                .map(EnrollmentJpaEntity::toDomain);
    }

    @Override
    public List<Enrollment> findByStatus(EnrollmentStatus status) {
        return springDataEnrollmentRepository.findByStatus(status)
                .stream()
                .map(EnrollmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Enrollment> findByEnrollmentIdAndUserIdAndStatus(
            Long enrollmentId,
            Long userId,
            EnrollmentStatus status
    ) {
        return springDataEnrollmentRepository.findByEnrollmentIdAndUserIdAndStatus(enrollmentId, userId, status)
                .map(EnrollmentJpaEntity::toDomain);
    }
}
