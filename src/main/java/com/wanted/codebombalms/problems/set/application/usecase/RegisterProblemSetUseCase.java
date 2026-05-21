package com.wanted.codebombalms.problems.set.application.usecase;

import com.wanted.codebombalms.problems.set.application.command.ProblemSetCreateCommandResult;
import com.wanted.codebombalms.problems.set.application.command.RegisterProblemSetCommand;

public interface RegisterProblemSetUseCase {

    ProblemSetCreateCommandResult handle(RegisterProblemSetCommand command);
}
