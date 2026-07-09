package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetBrief;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummaryPage;

import java.util.Optional;

public interface LoadProblemSetPort {
    ProblemSetSummaryPage loadActiveProblemSetsByCategory(
            Long categoryId,
            int page,
            int size,
            boolean popularSort
    );

    ProblemSetSummaryPage loadActiveProblemSets(
            int page,
            int size,
            boolean popularSort
    );

    // 챗봇 adapter용 단건 조회
    Optional<ProblemSetBrief> loadById(Long problemSetId);
}
