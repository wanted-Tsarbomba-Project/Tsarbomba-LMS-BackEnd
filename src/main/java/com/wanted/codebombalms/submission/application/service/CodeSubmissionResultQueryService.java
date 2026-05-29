package com.wanted.codebombalms.submission.application.service;

import com.wanted.codebombalms.submission.application.port.SubmissionResultQueryPort;
import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionResultQueryUseCase;
import com.wanted.codebombalms.submission.domain.model.CodeSubmissionResult;
import com.wanted.codebombalms.submission.domain.model.CodeSubmissionTestCaseResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeSubmissionResultQueryService implements CodeSubmissionResultQueryUseCase {

    private final SubmissionResultQueryPort submissionResultQueryPort;

    public CodeSubmissionResultQueryService(SubmissionResultQueryPort submissionResultQueryPort) {
        this.submissionResultQueryPort = submissionResultQueryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public CodeSubmissionResultView handle(Long submissionId, Long userId) {
        CodeSubmissionResult result = submissionResultQueryPort.getCodeSubmissionResult(submissionId, userId);

        return new CodeSubmissionResultView(
                result.submissionId(),
                result.problemId(),
                result.correct(),
                result.passedTestCount(),
                result.totalTestCount(),
                result.executionStatus(),
                result.errorMessage(),
                result.submittedAt(),
                result.testCaseResults()
                        .stream()
                        .map(this::toView)
                        .toList()
        );
    }

    private TestCaseResultView toView(CodeSubmissionTestCaseResult result) {
        if (Boolean.TRUE.equals(result.hidden())) {
            return new TestCaseResultView(
                    result.testCaseId(),
                    result.passed(),
                    true,
                    null,
                    null,
                    null
            );
        }

        return new TestCaseResultView(
                result.testCaseId(),
                result.passed(),
                false,
                result.actualOutput(),
                result.errorMessage(),
                result.executionTimeMs()
        );
    }
}
