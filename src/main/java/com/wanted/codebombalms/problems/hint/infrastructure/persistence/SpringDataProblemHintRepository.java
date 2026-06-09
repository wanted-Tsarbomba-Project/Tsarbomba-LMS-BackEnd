package com.wanted.codebombalms.problems.hint.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProblemHintRepository extends JpaRepository<ProblemHintJpaEntity, Long> {

    List<ProblemHintJpaEntity> findByProblem_ProblemIdAndProblem_StatusOrderByHintOrderAsc(
            Long problemId,
            String problemStatus
    );

    Optional<ProblemHintJpaEntity> findByHintIdAndProblem_ProblemId(Long hintId, Long problemId);
}
