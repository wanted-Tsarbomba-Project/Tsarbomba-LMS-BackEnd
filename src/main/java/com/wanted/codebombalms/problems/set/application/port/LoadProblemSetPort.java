package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.application.query.GetProblemSetsQuery;
import com.wanted.codebombalms.problems.set.application.query.ProblemSetCompletionStatus;
import com.wanted.codebombalms.problems.set.application.query.ProblemSetSort;
import com.wanted.codebombalms.problems.set.application.query.ProblemSetSortDirection;
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

    ProblemSetSummaryPage loadActiveProblemSets(GetProblemSetsQuery query);

    // 챗봇 adapter용 단건 조회
    Optional<ProblemSetBrief> loadById(Long problemSetId);
}
