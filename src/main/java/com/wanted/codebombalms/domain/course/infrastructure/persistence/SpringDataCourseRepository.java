package com.wanted.codebombalms.domain.course.infrastructure.persistence;

import com.wanted.codebombalms.domain.course.domain.model.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataCourseRepository extends JpaRepository<CourseJpaEntity, Long> {

    List<CourseJpaEntity> findByDeletedAtIsNull();

    List<CourseJpaEntity> findByStatusAndDeletedAtIsNull(CourseStatus status);

    Optional<CourseJpaEntity> findByCourseIdAndDeletedAtIsNull(Long courseId);

    Optional<CourseJpaEntity> findByCourseIdAndStatusAndDeletedAtIsNull(Long courseId, CourseStatus status);

    List<CourseJpaEntity> findByInstructorIdAndDeletedAtIsNull(Long instructorId);
}
