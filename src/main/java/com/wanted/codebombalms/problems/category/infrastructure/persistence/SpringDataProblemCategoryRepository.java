package com.wanted.codebombalms.problems.category.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataProblemCategoryRepository extends JpaRepository<ProblemCategoryJpaEntity, Long> {

    List<ProblemCategoryJpaEntity> findByStatus(String status);

    List<ProblemCategoryJpaEntity> findByStatusOrderByCategoryIdAsc(String status);

    boolean existsByCategoryIdAndStatus(Long categoryId, String status);

    Optional<ProblemCategoryJpaEntity> findByCategoryNameAndStatus(String categoryName, String status);
}
