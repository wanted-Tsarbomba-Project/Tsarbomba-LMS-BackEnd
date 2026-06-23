package com.wanted.codebombalms.problems.set.domain.model;

import java.util.List;

public record ProblemModification(
        Long problemId,
        String title,
        String content,
        String problemType,
        String difficulty,
        Integer point,
        Integer attemptLimit,
        Boolean isRetriable,
        Long hintId,
        String hint,
        String explanation,
        List<ProblemTestCaseModification> testCases
) {
}
