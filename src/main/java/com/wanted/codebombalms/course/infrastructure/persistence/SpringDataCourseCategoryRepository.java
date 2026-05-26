package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.CourseCategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataCourseCategoryRepository extends JpaRepository<CourseCategoryJpaEntity, Long> {

    List<CourseCategoryJpaEntity> findByStatusOrderByDisplayOrderAsc(CourseCategoryStatus status);

    Optional<CourseCategoryJpaEntity> findByCourseCategoryIdAndStatus(
            Long courseCategoryId,
            CourseCategoryStatus status
    );

    boolean existsByCourseCategoryIdAndStatus(Long courseCategoryId, CourseCategoryStatus status);
}
