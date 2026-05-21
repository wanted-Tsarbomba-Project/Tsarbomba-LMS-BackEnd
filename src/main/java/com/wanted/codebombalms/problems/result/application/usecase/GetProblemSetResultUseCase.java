package com.wanted.codebombalms.problems.result.application.usecase;

import com.wanted.codebombalms.problems.result.application.query.GetProblemSetResultQuery;
import com.wanted.codebombalms.problems.result.domain.model.ProblemSetResult;

public interface GetProblemSetResultUseCase {

    ProblemSetResult handle(GetProblemSetResultQuery query);
}
