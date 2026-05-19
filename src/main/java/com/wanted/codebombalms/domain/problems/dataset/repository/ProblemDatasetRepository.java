package com.wanted.codebombalms.domain.problems.dataset.repository;


import com.wanted.codebombalms.domain.problems.dataset.entitiy.ProblemDataset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ProblemDatasetRepository extends JpaRepository<ProblemDataset, Long> {
    Optional<ProblemDataset> findFirstByProblem_ProblemIdAndStatus(Long problemId, String status);

}
