package com.wanted.codebombalms.domain.problems.dataset.dto.response;

public record ProblemDatasetConnectResponse(
        Long problemId,
        Long datasetId,
        String startCode
) {
}
