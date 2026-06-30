package com.wanted.codebombalms.submission.infrastructure.persistence;

import com.wanted.codebombalms.submission.domain.model.CodeSubmissionTestCaseResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataSubmissionTestResultRepository
        extends JpaRepository<SubmissionTestResultJpaEntity, Long> {


    @Query("""
            select new com.wanted.codebombalms.submission.domain.model.CodeSubmissionTestCaseResult(
                testCase.testCaseId,
                result.passed,
                testCase.hidden,
                result.actualOutput,
                result.errorMessage,
                result.executionTimeMs
            )
            from SubmissionTestResultJpaEntity result
            join result.testCase testCase
            where result.submission.submissionId = :submissionId
            order by testCase.testOrder asc
            """)
    List<CodeSubmissionTestCaseResult> findResultDetailsBySubmissionId(
            @Param("submissionId") Long submissionId
    );
}
