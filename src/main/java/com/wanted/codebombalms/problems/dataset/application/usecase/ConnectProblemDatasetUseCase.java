package com.wanted.codebombalms.problems.dataset.application.usecase;

import com.wanted.codebombalms.problems.dataset.application.command.ConnectProblemDatasetCommand;

public interface ConnectProblemDatasetUseCase {

    ConnectProblemDatasetView handle(ConnectProblemDatasetCommand command);

    record ConnectProblemDatasetView(
            Long problemId,
            Long datasetId,
            String startCode
    ) {
    }
}
