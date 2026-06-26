package com.wanted.codebombalms.problems.set.domain.model;

import java.util.List;

public record ProblemSetSummaryPage(
        List<ProblemSetSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
