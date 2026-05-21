package com.wanted.codebombalms.problems.dataset.presentation.api.request;

import jakarta.validation.constraints.NotNull;

public record ProblemDatasetConnectRequest(
        @NotNull(message = "데이터셋 ID는 필수입니다.")
        Long datasetId
) {
}
