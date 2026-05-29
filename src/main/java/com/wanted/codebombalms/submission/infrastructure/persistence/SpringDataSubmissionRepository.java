package com.wanted.codebombalms.submission.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SpringDataSubmissionRepository extends JpaRepository<SubmissionJpaEntity, Long> {

    int countByUserIdAndProblem_ProblemId(Long userId, Long problemId);

    Optional<SubmissionJpaEntity> findTopByUserIdAndProblem_ProblemIdOrderBySubmittedAtDesc(
            Long userId,
            Long problemId
    );
    Optional<SubmissionJpaEntity> findBySubmissionIdAndUserId(Long submissionId, Long userId);
    boolean existsByProblem_ProblemSet_ProblemSetId(Long problemSetId);

    Page<SubmissionJpaEntity> findByProblem_ProblemIdAndSubmittedCodeIsNotNullOrderBySubmittedAtDesc(
            Long problemId,
            Pageable pageable
    );

    Page<SubmissionJpaEntity> findByUserIdAndProblem_ProblemIdAndSubmittedCodeIsNotNullOrderBySubmittedAtDesc(
            Long userId,
            Long problemId,
            Pageable pageable
    );
}
