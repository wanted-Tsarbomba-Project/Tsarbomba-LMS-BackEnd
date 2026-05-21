package com.wanted.codebombalms.problems.problem.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProblemRepository extends JpaRepository<ProblemJpaEntity, Long> {

    List<ProblemJpaEntity> findByProblemSet_Category_CategoryIdAndStatusOrderByProblemOrderAsc(
            Long categoryId,
            String status
    );

    Optional<ProblemJpaEntity> findByProblemSet_ProblemSetIdAndProblemOrderAndStatus(
            Long problemSetId,
            Integer problemOrder,
            String status
    );

    List<ProblemJpaEntity> findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(
            Long problemSetId,
            String status
    );

    Optional<ProblemJpaEntity> findTopByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderDesc(
            Long problemSetId,
            String status
    );

    Optional<ProblemJpaEntity> findByProblemIdAndProblemSet_ProblemSetId(
            Long problemId,
            Long problemSetId
    );
}
