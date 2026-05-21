package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProblemSetRepository extends JpaRepository<ProblemSetJpaEntity, Long> {

    List<ProblemSetJpaEntity> findByCategory_CategoryIdAndStatusOrderByProblemSetIdAsc(
            Long categoryId,
            String status
    );
}
