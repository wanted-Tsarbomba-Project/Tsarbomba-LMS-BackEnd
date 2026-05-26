package com.wanted.codebombalms.problems.testcase.presentation.api.response;

import com.wanted.codebombalms.problems.testcase.application.usecase.ProblemTestCaseCommandUseCase;

public record ProblemTestCaseResponse(
        Long testCaseId,
        Long problemId,
        String testCode,
        String expectedResult,
        Integer testOrder,
        Boolean isHidden,
        Integer timeoutMs,
        String status
) {

    public static ProblemTestCaseResponse from(ProblemTestCaseCommandUseCase.TestCaseView view) {
        return new ProblemTestCaseResponse(
                view.testCaseId(),
                view.problemId(),
                view.testCode(),
                view.expectedResult(),
                view.testOrder(),
                view.hidden(),
                view.timeoutMs(),
                view.status()
        );
    }
}