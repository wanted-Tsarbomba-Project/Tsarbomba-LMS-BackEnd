package com.wanted.codebombalms.enrollment.infrastructure.persistence;

import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.enrollment.domain.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EnrollmentRepositoryAdapter implements EnrollmentRepository {

    private final SpringDataEnrollmentRepository springDataEnrollmentRepository;

    @Override
    public Enrollment save(Enrollment enrollment) {
        EnrollmentJpaEntity entity = enrollment.getEnrollmentId() == null
                ? EnrollmentJpaEntity.from(enrollment)
                : springDataEnrollmentRepository.findById(enrollment.getEnrollmentId())
                .map(found -> {
                    found.apply(enrollment);
                    return found;
                })
                .orElseGet(() -> EnrollmentJpaEntity.from(enrollment));

        return springDataEnrollmentRepository.save(entity).toDomain();
    }

    @Override
    public boolean existsByCourseIdAndUserIdAndStatus(Long courseId, Long userId, EnrollmentStatus status) {
        return springDataEnrollmentRepository.existsByCourseIdAndUserIdAndStatus(courseId, userId, status);
    }

    @Override
    public boolean existsByCourseIdAndUserId(Long courseId, Long userId) {
        return springDataEnrollmentRepository.existsByCourseIdAndUserId(courseId, userId);
    }

    @Override
    public List<Enrollment> findByUserIdAndStatus(Long userId, EnrollmentStatus status) {
        return springDataEnrollmentRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(EnrollmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Enrollment> findByCourseIdAndStatus(Long courseId, EnrollmentStatus status) {
        return springDataEnrollmentRepository.findByCourseIdAndStatus(courseId, status)
                .stream()
                .map(EnrollmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Enrollment> findByCourseIdAndStatus(Long courseId, EnrollmentStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return springDataEnrollmentRepository.findByCourseIdAndStatusOrderByUserIdAsc(courseId, status, pageable)
                .stream()
                .map(EnrollmentJpaEntity::toDomain)
                .toList();
    }

    @Override
    public long countByCourseIdAndStatus(Long courseId, EnrollmentStatus status) {
        return springDataEnrollmentRepository.countByCourseIdAndStatus(courseId, status);
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
        return springDataEnrollmentRepository.findByCourseIdAndUserIdAndStatus(courseId, userId, status)
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
