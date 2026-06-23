package com.wanted.codebombalms.problems.dataset.presentation.api.response;

import com.wanted.codebombalms.problems.dataset.application.usecase.UploadProblemDatasetUseCase.UploadProblemDatasetView;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemDatasetUploadResponse(
        @Schema(description = "업로드된 데이터셋 ID", example = "3001")
        Long datasetId,

        @Schema(description = "사용자가 업로드한 원본 파일명", example = "employee_performance.csv")
        String originalFileName,

        @Schema(description = "데이터셋 상태", example = "ACTIVE")
        String status
) {
    public ProblemDatasetUploadResponse(UploadProblemDatasetView result) {
        this(
                result.datasetId(),
                result.originalFileName(),
                result.status()
        );
    }
}
