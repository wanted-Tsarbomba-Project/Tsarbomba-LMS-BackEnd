package com.wanted.codebombalms.problems.set.presentation.api.request;

public record ProblemCreateRequest(
        String title,
        String content,
        Integer point,
        String startCode,
        String answer,
        String hint,
        String explanation
) {
}
