package com.wanted.codebombalms.problems.progress.application.usecase;

import com.wanted.codebombalms.problems.progress.application.query.GetProblemProgressQuery;
import com.wanted.codebombalms.problems.progress.domain.model.ProblemProgress;

public interface GetProblemProgressUseCase {

    ProblemProgress handle(GetProblemProgressQuery query);
}
