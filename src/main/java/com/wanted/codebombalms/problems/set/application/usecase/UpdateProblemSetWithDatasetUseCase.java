package com.wanted.codebombalms.problems.set.application.usecase;

import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.set.application.command.ProblemSetUpdateCommandResult;
import com.wanted.codebombalms.problems.set.application.command.UpdateProblemSetCommand;

public interface UpdateProblemSetWithDatasetUseCase {

    ProblemSetUpdateCommandResult handle(
            UpdateProblemSetCommand problemSetCommand,
            UploadProblemDatasetCommand datasetCommand
    );
}
