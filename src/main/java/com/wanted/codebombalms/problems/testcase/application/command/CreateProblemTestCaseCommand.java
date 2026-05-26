package com.wanted.codebombalms.problems.testcase.application.command;

public record CreateProblemTestCaseCommand(
        Long problemId,
        String testCode,
        String expectedResult,
        Integer testOrder,
        Boolean hidden,
        Integer timeoutMs
) {
}