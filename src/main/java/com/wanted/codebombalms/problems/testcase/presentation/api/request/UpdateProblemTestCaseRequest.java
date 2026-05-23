package com.wanted.codebombalms.problems.testcase.presentation.api.request;

public record UpdateProblemTestCaseRequest(
        String testCode,
        String expectedResult,
        Integer testOrder,
        Integer score,
        Boolean isHidden,
        Integer timeoutMs
) {
}
