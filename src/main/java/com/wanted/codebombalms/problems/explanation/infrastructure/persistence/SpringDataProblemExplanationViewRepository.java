package com.wanted.codebombalms.problems.explanation.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SpringDataProblemExplanationViewRepository
        extends JpaRepository<ProblemExplanationViewJpaEntity, Long> {

    boolean existsByUserIdAndProblemId(Long userId, Long problemId);

    List<ProblemExplanationViewJpaEntity> findByUserIdAndProblemIdIn(
            Long userId,
            Collection<Long> problemIds
    );
}
