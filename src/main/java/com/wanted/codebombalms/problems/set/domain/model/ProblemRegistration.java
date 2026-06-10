package com.wanted.codebombalms.problems.set.domain.model;

import java.util.List;

public record ProblemRegistration(
        String title,
        String content,
        String problemType,
        String difficulty,
        Integer point,
        Integer attemptLimit,
        Boolean isRetriable,
        String hint,
        String explanation,
        List<ProblemTestCaseRegistration> testCases
) {
}
