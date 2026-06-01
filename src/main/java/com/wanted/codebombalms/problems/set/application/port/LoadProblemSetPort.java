package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetBrief;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;

import java.util.List;
import java.util.Optional;

public interface LoadProblemSetPort {

    List<ProblemSetSummary> loadActiveProblemSetsByCategory(Long categoryId);

    List<ProblemSetSummary> loadActiveProblemSets();

    // 챗봇 adapter용 단건 조회
    Optional<ProblemSetBrief> loadById(Long problemSetId);
}
