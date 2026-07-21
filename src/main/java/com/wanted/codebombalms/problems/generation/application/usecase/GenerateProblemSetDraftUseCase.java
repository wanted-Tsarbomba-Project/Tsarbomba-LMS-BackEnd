package com.wanted.codebombalms.problems.generation.application.usecase;

import com.wanted.codebombalms.problems.generation.application.command.GenerateProblemSetDraftCommand;
import com.wanted.codebombalms.problems.generation.application.result.ProblemSetDraftResult;

public interface GenerateProblemSetDraftUseCase {

    ProblemSetDraftResult generate(GenerateProblemSetDraftCommand command);
}
