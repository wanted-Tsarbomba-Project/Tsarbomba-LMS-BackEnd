package com.wanted.codebombalms.problems.set.application.usecase;

import com.wanted.codebombalms.problems.set.application.command.DeleteProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.command.ProblemSetDeleteCommandResult;

public interface DeleteProblemSetUseCase {

    ProblemSetDeleteCommandResult handle(DeleteProblemSetCommand command);
}
