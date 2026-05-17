package com.wanted.codebombalms.domain.problems.category.repository;

import com.wanted.codebombalms.domain.problems.category.entity.ProblemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemCategoryRepository extends JpaRepository<ProblemCategory, Long> {
    List<ProblemCategory> findByStatus(String status);

    List<ProblemCategory> findByStatusOrderByCategoryIdAsc(String status);

    boolean existsByCategoryIdAndStatus(Long categoryId, String status);

    Optional<ProblemCategory> findByCategoryNameAndStatus(String categoryName, String status);
}
