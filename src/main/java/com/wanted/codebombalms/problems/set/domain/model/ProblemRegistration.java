package com.wanted.codebombalms.problems.set.domain.model;

public record ProblemRegistration(
        String title,
        String content,
        Integer point,
        String answer,
        String hint,
        String explanation
) {
}