package com.wanted.codebombalms.problems.generation.domain;

import java.util.List;

public record GeneratedProblemDraft(
        String title,
        int point,
        String content,
        String startCode,
        String hint,
        String explanation,
        List<GeneratedTestCaseDraft> testCases
) {
}
