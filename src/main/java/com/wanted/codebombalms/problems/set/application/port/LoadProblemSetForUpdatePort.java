package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetForUpdateUseCase.ProblemSetForUpdateView;

public interface LoadProblemSetForUpdatePort {

    ProblemSetForUpdateView load(Long problemSetId);
}
