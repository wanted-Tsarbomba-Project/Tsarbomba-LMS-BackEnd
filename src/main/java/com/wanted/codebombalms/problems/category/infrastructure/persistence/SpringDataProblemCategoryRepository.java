package com.wanted.codebombalms.problems.category.infrastructure.persistence;

import com.wanted.codebombalms.problems.category.domain.model.ProblemCategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataProblemCategoryRepository extends JpaRepository<ProblemCategoryJpaEntity, Long> {

    List<ProblemCategoryJpaEntity> findByStatus(ProblemCategoryStatus status);

    List<ProblemCategoryJpaEntity> findByStatusOrderByCategoryIdAsc(ProblemCategoryStatus status);

    boolean existsByCategoryIdAndStatus(Long categoryId, ProblemCategoryStatus status);

    Optional<ProblemCategoryJpaEntity> findByCategoryNameAndStatus(
            String categoryName,
            ProblemCategoryStatus status
    );
}
