package com.wanted.codebombalms.problems.set.domain.model;

public record ProblemRegistration(
        String title,
        String content,
        String problemType,
        String difficulty,
        Integer point,
        Integer attemptLimit,
        Boolean isRetriable,
        String answer,
        String hint,
        String explanation
) {
}
