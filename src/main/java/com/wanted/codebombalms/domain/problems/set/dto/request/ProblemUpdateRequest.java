package com.wanted.codebombalms.domain.problems.set.dto.request;

public record ProblemUpdateRequest(
        Long problemId,
        String title,
        String content,
        Integer point,
        String startCode,
        String answer,
        Long hintId,
        String hint,
        String explanation
) {
}
