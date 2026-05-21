package com.wanted.codebombalms.submission.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataSubmissionRepository extends JpaRepository<SubmissionJpaEntity, Long> {

    int countByUserIdAndProblem_ProblemId(Long userId, Long problemId);

    Optional<SubmissionJpaEntity> findTopByUserIdAndProblem_ProblemIdOrderBySubmittedAtDesc(
            Long userId,
            Long problemId
    );

    boolean existsByProblem_ProblemSet_ProblemSetId(Long problemSetId);
}
