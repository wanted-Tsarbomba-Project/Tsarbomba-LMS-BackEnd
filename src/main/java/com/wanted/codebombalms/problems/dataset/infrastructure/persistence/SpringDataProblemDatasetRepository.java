package com.wanted.codebombalms.problems.dataset.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProblemDatasetRepository extends JpaRepository<ProblemDatasetJpaEntity, Long> {

    Optional<ProblemDatasetJpaEntity> findFirstByProblem_ProblemIdAndStatus(Long problemId, String status);
}
