package com.wanted.codebombalms.domain.problems.set.dto.request;

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
