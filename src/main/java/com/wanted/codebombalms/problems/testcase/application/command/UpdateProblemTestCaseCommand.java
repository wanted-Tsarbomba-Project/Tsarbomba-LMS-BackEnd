package com.wanted.codebombalms.problems.testcase.application.command;

public record UpdateProblemTestCaseCommand(
        Long testCaseId,
        String testCode,
        String expectedResult,
        Integer testOrder,
        Boolean hidden,
        Integer timeoutMs
) {
}