package com.wanted.codebombalms.problems.dataset.application.usecase;

import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;

public interface UploadProblemDatasetUseCase {

    UploadProblemDatasetView handle(UploadProblemDatasetCommand command);

    record UploadProblemDatasetView(
            Long datasetId,
            String originalFileName,
            String status
    ) {
    }
}
