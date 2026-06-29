package com.wanted.codebombalms.problems.set.application.usecase;

import com.wanted.codebombalms.problems.set.application.query.GetProblemSetsQuery;

import java.time.LocalDateTime;
import java.util.List;

public interface GetProblemSetsUseCase {

    ProblemSetPageView handle(GetProblemSetsQuery query);

    record ProblemSetPageView(
            List<ProblemSetSummaryView> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
    }

    record ProblemSetSummaryView(
            Long problemSetId,
            Integer problemNumber,
            String title,
            String description,
            String difficulty,
            Double accuracyRate,
            LocalDateTime createdAt
    ) {
    }
}
