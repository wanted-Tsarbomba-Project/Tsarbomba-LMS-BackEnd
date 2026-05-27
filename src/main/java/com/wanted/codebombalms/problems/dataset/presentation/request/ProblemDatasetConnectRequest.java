package com.wanted.codebombalms.problems.dataset.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ProblemDatasetConnectRequest(
        @Schema(description = "문제에 연결할 데이터셋 ID", example = "3001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "데이터셋 ID는 필수입니다.")
        Long datasetId
) {
}
