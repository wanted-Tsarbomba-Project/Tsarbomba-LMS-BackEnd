package com.wanted.codebombalms.problems.set.application.usecase;

import com.wanted.codebombalms.problems.set.application.query.GetProblemSetsQuery;

import java.time.LocalDateTime;
import java.util.List;

public interface GetProblemSetsUseCase {

    List<ProblemSetSummaryView> handle(GetProblemSetsQuery query);

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
