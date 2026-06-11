package com.wanted.codebombalms.submission.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wanted.codebombalms.submission.application.port.ProblemSubmissionMetricPort.ProblemWrongRateMetric;

import java.util.List;
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

    boolean existsByUserIdAndProblem_ProblemIdAndIsCorrectTrue(
            Long userId,
            Long problemId
    );

    @Query("""
            select new com.wanted.codebombalms.submission.application.port.ProblemSubmissionMetricPort$ProblemWrongRateMetric(
                s.problem.problemId,
                count(s.submissionId),
                sum(case when s.isCorrect = false then 1 else 0 end)
            )
            from SubmissionJpaEntity s
            where s.problem.status = 'ACTIVE'
            group by s.problem.problemId
            having count(s.submissionId) >= :minSampleCount
            """)
    List<ProblemWrongRateMetric> findProblemWrongRateMetrics(@Param("minSampleCount") Long minSampleCount);
}
