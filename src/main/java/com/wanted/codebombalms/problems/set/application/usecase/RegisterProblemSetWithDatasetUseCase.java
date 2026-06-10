package com.wanted.codebombalms.problems.set.application.usecase;

import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.set.application.command.RegisterProblemSetCommand;

public interface RegisterProblemSetWithDatasetUseCase {

    ProblemSetWithDatasetCreateView handle(
            RegisterProblemSetCommand problemSetCommand,
            UploadProblemDatasetCommand datasetCommand
    );

    record ProblemSetWithDatasetCreateView(
            Long problemSetId,
            Long datasetId,
            String title,
            String categoryName,
            Integer totalProblemCount,
            Integer createdProblemCount,
            Integer createdTestCaseCount,
            String datasetFileName,
            String startCode
    ) {
    }
}
