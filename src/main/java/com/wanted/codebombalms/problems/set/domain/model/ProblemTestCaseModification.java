package com.wanted.codebombalms.problems.set.domain.model;

public record ProblemTestCaseModification(
        Long testCaseId,
        String testCode,
        Boolean hidden,
        Integer timeoutMs
) {
}
