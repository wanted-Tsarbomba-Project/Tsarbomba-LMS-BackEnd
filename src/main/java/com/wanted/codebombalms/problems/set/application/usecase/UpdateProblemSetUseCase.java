package com.wanted.codebombalms.problems.set.application.usecase;

import com.wanted.codebombalms.problems.set.application.command.ProblemSetUpdateCommandResult;
import com.wanted.codebombalms.problems.set.application.command.UpdateProblemSetCommand;

public interface UpdateProblemSetUseCase {

    ProblemSetUpdateCommandResult handle(UpdateProblemSetCommand command);
}
