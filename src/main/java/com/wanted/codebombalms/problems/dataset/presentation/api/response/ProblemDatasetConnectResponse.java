package com.wanted.codebombalms.problems.dataset.presentation.api.response;

import com.wanted.codebombalms.problems.dataset.application.usecase.ConnectProblemDatasetUseCase.ConnectProblemDatasetView;

public record ProblemDatasetConnectResponse(
        Long problemId,
        Long datasetId,
        String startCode
) {
    public ProblemDatasetConnectResponse(ConnectProblemDatasetView result) {
        this(
                result.problemId(),
                result.datasetId(),
                result.startCode()
        );
    }
}
