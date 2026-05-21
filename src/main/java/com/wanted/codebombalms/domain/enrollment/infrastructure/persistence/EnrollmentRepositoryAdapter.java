package com.wanted.codebombalms.domain.enrollment.infrastructure.persistence;

import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.course.infrastructure.persistence.CourseJpaEntity;
import com.wanted.codebombalms.domain.course.infrastructure.persistence.SpringDataCourseRepository;
import com.wanted.codebombalms.domain.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.domain.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.domain.enrollment.domain.repository.EnrollmentRepository;
import com.wanted.codebombalms.domain.user.entity.User;
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
    public boolean existsByCourseAndStudentAndStatus(Course course, User student, EnrollmentStatus status) {
        return springDataEnrollmentRepository.existsByCourse_CourseIdAndStudentAndStatus(
                course.getCourseId(),
                student,
                status
        );
    }

    @Override
    public List<Enrollment> findByStudentAndStatus(User student, EnrollmentStatus status) {
        return springDataEnrollmentRepository.findByStudentAndStatus(student, status)
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
    public Optional<Enrollment> findByEnrollmentIdAndStudentAndStatus(
            Long enrollmentId,
            User student,
            EnrollmentStatus status
    ) {
        return springDataEnrollmentRepository.findByEnrollmentIdAndStudentAndStatus(enrollmentId, student, status)
                .map(EnrollmentJpaEntity::toDomain);
    }
}
