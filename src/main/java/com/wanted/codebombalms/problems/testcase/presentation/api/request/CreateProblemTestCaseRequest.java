package com.wanted.codebombalms.problems.testcase.presentation.api.request;

public record CreateProblemTestCaseRequest(
        String testCode,
        String expectedResult,
        Integer testOrder,
        Boolean isHidden,
        Integer timeoutMs
) {
}