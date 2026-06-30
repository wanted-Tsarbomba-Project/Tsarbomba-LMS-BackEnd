package com.wanted.codebombalms.submission.infrastructure.persistence;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.submission.application.port.SubmissionResultQueryPort;
import com.wanted.codebombalms.submission.domain.model.CodeSubmissionResult;
import com.wanted.codebombalms.submission.domain.model.CodeSubmissionTestCaseResult;
import com.wanted.codebombalms.submission.exception.SubmissionErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubmissionResultQueryPersistenceAdapter implements SubmissionResultQueryPort {

    private final SpringDataSubmissionRepository submissionRepository;
    private final SpringDataSubmissionTestResultRepository submissionTestResultRepository;

    public SubmissionResultQueryPersistenceAdapter(
            SpringDataSubmissionRepository submissionRepository,
            SpringDataSubmissionTestResultRepository submissionTestResultRepository
    ) {
        this.submissionRepository = submissionRepository;
        this.submissionTestResultRepository = submissionTestResultRepository;
    }

    @Override
    public CodeSubmissionResult getCodeSubmissionResult(Long submissionId, Long userId) {
        SubmissionJpaEntity submission = submissionRepository.findBySubmissionIdAndUserId(submissionId, userId)
                .orElseThrow(() -> new NotFoundException(SubmissionErrorCode.SUBMISSION_NOT_FOUND));

        List<CodeSubmissionTestCaseResult> testCaseResults =
                submissionTestResultRepository
                        .findResultDetailsBySubmissionId(submissionId);

        return new CodeSubmissionResult(
                submission.getSubmissionId(),
                submission.getProblem().getProblemId(),
                submission.getSubmittedCode(),
                submission.getCorrect(),
                submission.getPassedTestCount(),
                submission.getTotalTestCount(),
                submission.getExecutionStatus(),
                submission.getErrorMessage(),
                submission.getSubmittedAt(),
                testCaseResults
        );
    }

}
