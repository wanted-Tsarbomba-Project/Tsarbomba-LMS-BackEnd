package com.wanted.codebombalms.problems.set.domain.model;

public record ProblemModification(
        Long problemId,
        String title,
        String content,
        Integer point,
        String answer,
        Long hintId,
        String hint,
        String explanation
) {
}
