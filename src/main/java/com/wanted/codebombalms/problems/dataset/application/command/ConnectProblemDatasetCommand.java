package com.wanted.codebombalms.problems.dataset.application.command;

public record ConnectProblemDatasetCommand(
        Long problemId,
        Long datasetId
) {
}
