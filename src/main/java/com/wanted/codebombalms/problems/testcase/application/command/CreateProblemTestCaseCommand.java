package com.wanted.codebombalms.problems.testcase.application.command;

public record CreateProblemTestCaseCommand(
        Long problemId,
        String testCode,
        String expectedResult,
        Integer testOrder,
        Integer score,
        Boolean hidden,
        Integer timeoutMs
) {
}