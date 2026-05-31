package com.wanted.codebombalms.submission.infrastructure.persistence;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.testcase.infrastructure.persistence.ProblemTestCaseJpaEntity;
import com.wanted.codebombalms.submission.application.port.SubmissionCommandPort;
import com.wanted.codebombalms.submission.domain.model.CodeSubmission;
import com.wanted.codebombalms.submission.domain.model.SubmissionTestResult;
import com.wanted.codebombalms.submission.exception.SubmissionErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SubmissionCommandPersistenceAdapter implements SubmissionCommandPort {

    private final SpringDataSubmissionRepository submissionRepository;
    private final SpringDataSubmissionTestResultRepository submissionTestResultRepository;
    private final EntityManager entityManager;


    @Override
    public int countAttempts(Long userId, Long problemId) {
        return submissionRepository.countByUserIdAndProblem_ProblemId(userId, problemId);
    }

    @Override
    public Long saveCodeSubmission(CodeSubmission submission) {
        ProblemJpaEntity problem = entityManager.getReference(
                ProblemJpaEntity.class,
                submission.problemId()
        );

        SubmissionJpaEntity submissionEntity = new SubmissionJpaEntity(
                submission.userId(),
                problem,
                submission.submittedCode(),
                submission.correct(),
                submission.attemptNo(),
                submission.passedTestCount(),
                submission.totalTestCount(),
                submission.executionStatus(),
                submission.errorMessage()
        );

        return submissionRepository.save(submissionEntity).getSubmissionId();
    }

    @Override
    public void saveTestResults(Long submissionId, List<SubmissionTestResult> testResults) {
        SubmissionJpaEntity submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException(SubmissionErrorCode.SUBMISSION_NOT_FOUND));

        List<SubmissionTestResultJpaEntity> entities = testResults.stream()
                .map(testResult -> toEntity(submission, testResult))
                .toList();

        submissionTestResultRepository.saveAll(entities);
    }

    private SubmissionTestResultJpaEntity toEntity(
            SubmissionJpaEntity submission,
            SubmissionTestResult testResult
    ) {
        ProblemTestCaseJpaEntity testCase = entityManager.getReference(
                ProblemTestCaseJpaEntity.class,
                testResult.testCaseId()
        );

        return new SubmissionTestResultJpaEntity(
                submission,
                testCase,
                testResult.passed(),
                testResult.actualOutput(),
                testResult.errorMessage(),
                testResult.executionTimeMs()
        );
    }
}
