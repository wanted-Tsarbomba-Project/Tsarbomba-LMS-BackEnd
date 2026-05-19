package com.wanted.codebombalms.domain.submission.repository;

import com.wanted.codebombalms.domain.submission.entitiy.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    int countByUserIdAndProblem_ProblemId(Long userId, Long problemId);

    Optional<Submission> findTopByUserIdAndProblem_ProblemIdOrderBySubmittedAtDesc(
            Long userId,
            Long problemId
    );

    boolean existsByProblem_ProblemSet_ProblemSetId(Long problemSetId);
}
