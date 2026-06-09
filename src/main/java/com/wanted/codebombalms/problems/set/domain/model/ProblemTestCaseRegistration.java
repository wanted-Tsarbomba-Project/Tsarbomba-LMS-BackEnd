package com.wanted.codebombalms.problems.set.domain.model;

public record ProblemTestCaseRegistration(
        String testCode,
        Boolean hidden,
        Integer timeoutMs
) {
}
