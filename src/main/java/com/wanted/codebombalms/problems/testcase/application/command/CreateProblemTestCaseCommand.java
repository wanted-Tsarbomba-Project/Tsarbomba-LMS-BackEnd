package com.wanted.codebombalms.problems.testcase.application.command;

public record CreateProblemTestCaseCommand(
        Long problemId,
        String testCode,
        Integer testOrder,
        Boolean hidden,
        Integer timeoutMs
) {
}
