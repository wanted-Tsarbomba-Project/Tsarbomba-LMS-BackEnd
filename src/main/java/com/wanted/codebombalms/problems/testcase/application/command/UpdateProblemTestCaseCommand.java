package com.wanted.codebombalms.problems.testcase.application.command;

public record UpdateProblemTestCaseCommand(
        Long testCaseId,
        String testCode,
        String expectedResult,
        Integer testOrder,
        Integer score,
        Boolean hidden,
        Integer timeoutMs
) {
}
