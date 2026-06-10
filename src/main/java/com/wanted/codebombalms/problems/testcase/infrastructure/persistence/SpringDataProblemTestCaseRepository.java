package com.wanted.codebombalms.problems.testcase.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataProblemTestCaseRepository extends JpaRepository<ProblemTestCaseJpaEntity, Long> {
    List<ProblemTestCaseJpaEntity> findByProblem_ProblemIdAndStatusOrderByTestOrderAsc(
            Long problemId,
            String status
    );

    List<ProblemTestCaseJpaEntity> findByProblem_ProblemIdInAndStatus(
            List<Long> problemIds,
            String status
    );

    Optional<ProblemTestCaseJpaEntity> findByTestCaseIdAndStatus(
            Long testCaseId,
            String status
    );

    boolean existsByProblem_ProblemIdAndTestOrderAndStatus(
            Long problemId,
            Integer testOrder,
            String status
    );

    boolean existsByProblem_ProblemIdAndTestOrderAndStatusAndTestCaseIdNot(
            Long problemId,
            Integer testOrder,
            String status,
            Long testCaseId
    );

    List<ProblemTestCaseJpaEntity>
    findByProblem_ProblemIdInAndStatusOrderByProblem_ProblemIdAscTestOrderAsc(
            List<Long> problemIds,
            String status
    );
}
