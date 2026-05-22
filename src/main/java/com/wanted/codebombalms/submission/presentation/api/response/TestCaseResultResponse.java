package com.wanted.codebombalms.submission.presentation.api.response;

import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionResultQueryUseCase.TestCaseResultView;

public record TestCaseResultResponse(
        Long testCaseId,
        Boolean isPassed,
        Boolean isHidden,
        String actualOutput,
        String errorMessage,
        Integer executionTimeMs,
        Integer score
) {

    public TestCaseResultResponse(TestCaseResultView result) {
        this(
                result.testCaseId(),
                result.passed(),
                result.hidden(),
                result.actualOutput(),
                result.errorMessage(),
                result.executionTimeMs(),
                result.score()
        );
    }
}
