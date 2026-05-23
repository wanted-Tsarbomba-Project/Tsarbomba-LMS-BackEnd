package com.wanted.codebombalms.submission.infrastructure.persistence;

import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.problems.testcase.infrastructure.persistence.ProblemTestCaseJpaEntity;
import com.wanted.codebombalms.problems.testcase.infrastructure.persistence.SpringDataProblemTestCaseRepository;
import com.wanted.codebombalms.submission.application.port.SubmissionCommandPort;
import com.wanted.codebombalms.submission.domain.model.CodeSubmission;
import com.wanted.codebombalms.submission.domain.model.SubmissionTestResult;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubmissionCommandPersistenceAdapter implements SubmissionCommandPort {

    private final SpringDataSubmissionRepository submissionRepository;
    private final SpringDataSubmissionTestResultRepository submissionTestResultRepository;
    private final SpringDataProblemRepository problemRepository;
    private final SpringDataProblemTestCaseRepository problemTestCaseRepository;

    public SubmissionCommandPersistenceAdapter(
            SpringDataSubmissionRepository submissionRepository,
            SpringDataSubmissionTestResultRepository submissionTestResultRepository,
            SpringDataProblemRepository problemRepository,
            SpringDataProblemTestCaseRepository problemTestCaseRepository
    ) {
        this.submissionRepository = submissionRepository;
        this.submissionTestResultRepository = submissionTestResultRepository;
        this.problemRepository = problemRepository;
        this.problemTestCaseRepository = problemTestCaseRepository;
    }

    @Override
    public int countAttempts(Long userId, Long problemId) {
        return submissionRepository.countByUserIdAndProblem_ProblemId(userId, problemId);
    }

    @Override
    public Long saveCodeSubmission(CodeSubmission submission) {
        ProblemJpaEntity problem = problemRepository.findById(submission.problemId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        SubmissionJpaEntity submissionEntity = new SubmissionJpaEntity(
                submission.userId(),
                problem,
                submission.submittedCode(),
                submission.correct(),
                submission.earnedScore(),
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
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        List<SubmissionTestResultJpaEntity> entities = testResults.stream()
                .map(testResult -> toEntity(submission, testResult))
                .toList();

        submissionTestResultRepository.saveAll(entities);
    }

    private SubmissionTestResultJpaEntity toEntity(
            SubmissionJpaEntity submission,
            SubmissionTestResult testResult
    ) {
        ProblemTestCaseJpaEntity testCase = problemTestCaseRepository.findById(testResult.testCaseId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_TEST_CASE_NOT_FOUND));

        return new SubmissionTestResultJpaEntity(
                submission,
                testCase,
                testResult.passed(),
                testResult.actualOutput(),
                testResult.errorMessage(),
                testResult.executionTimeMs(),
                testResult.score()
        );
    }
}
