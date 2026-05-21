package com.wanted.codebombalms.problems.set.application.usecase;

import com.wanted.codebombalms.problems.set.application.query.EnterProblemSetQuery;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetEntry;

public interface EnterProblemSetUseCase {

    ProblemSetEntry handle(EnterProblemSetQuery query);
}
