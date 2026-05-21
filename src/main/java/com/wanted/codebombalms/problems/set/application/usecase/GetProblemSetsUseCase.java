package com.wanted.codebombalms.problems.set.application.usecase;

import com.wanted.codebombalms.problems.set.application.query.GetProblemSetsQuery;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;

import java.util.List;

public interface GetProblemSetsUseCase {

    List<ProblemSetSummary> handle(GetProblemSetsQuery query);
}
