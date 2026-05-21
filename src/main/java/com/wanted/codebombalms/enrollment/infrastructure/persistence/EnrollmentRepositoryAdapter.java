package com.wanted.codebombalms.enrollment.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
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
        CourseJpaEntity courseEntity = springDataCourseRepository.findById(enrollment.getCourse().getCourseId())
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
    public boolean existsByCourseAndStudentIdAndStatus(Course course, Long studentId, EnrollmentStatus status) {
        return springDataEnrollmentRepository.existsByCourse_CourseIdAndStudentIdAndStatus(
                course.getCourseId(),
                studentId,
                status
        );
    }

    @Override
    public List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status) {
        return springDataEnrollmentRepository.findByStudentIdAndStatus(studentId, status)
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
    public Optional<Enrollment> findByEnrollmentIdAndStudentIdAndStatus(
            Long enrollmentId,
            Long studentId,
            EnrollmentStatus status
    ) {
        return springDataEnrollmentRepository.findByEnrollmentIdAndStudentIdAndStatus(enrollmentId, studentId, status)
                .map(EnrollmentJpaEntity::toDomain);
    }
}
