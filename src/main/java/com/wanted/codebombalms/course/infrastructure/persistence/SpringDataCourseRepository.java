package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataCourseRepository extends JpaRepository<CourseJpaEntity, Long> {

    List<CourseJpaEntity> findByDeletedAtIsNull();

    List<CourseJpaEntity> findByStatusAndDeletedAtIsNull(CourseStatus status);

    List<CourseJpaEntity> findByCourseCategory_CourseCategoryIdAndStatusAndDeletedAtIsNull(
            Long courseCategoryId,
            CourseStatus status
    );

    Optional<CourseJpaEntity> findByCourseIdAndDeletedAtIsNull(Long courseId);

    Optional<CourseJpaEntity> findByCourseIdAndStatusAndDeletedAtIsNull(Long courseId, CourseStatus status);

    List<CourseJpaEntity> findByInstructorIdAndDeletedAtIsNull(Long instructorId);
}
