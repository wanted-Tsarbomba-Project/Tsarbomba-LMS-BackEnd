package com.wanted.codebombalms.problems.testcase.application.command;

public record UpdateProblemTestCaseCommand(
        Long testCaseId,
        String testCode,
        Integer testOrder,
        Boolean hidden,
        Integer timeoutMs
) {
}
