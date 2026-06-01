package com.wanted.codebombalms.problems.set.domain.model;

public record ProblemModification(
        Long problemId,
        String title,
        String content,
        String problemType,
        String difficulty,
        Integer point,
        Integer attemptLimit,
        Boolean isRetriable,
        String answer,
        Long hintId,
        String hint,
        String explanation
) {
}
