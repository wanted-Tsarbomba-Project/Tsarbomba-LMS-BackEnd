package com.wanted.codebombalms.problems.dataset.presentation.api.response;

import com.wanted.codebombalms.problems.dataset.application.usecase.UploadProblemDatasetUseCase.UploadProblemDatasetView;

public record ProblemDatasetUploadResponse(
        Long datasetId,
        String originalFileName,
        String storedFileName,
        String fileUrl,
        String filePath,
        String status
) {
    public ProblemDatasetUploadResponse(UploadProblemDatasetView result) {
        this(
                result.datasetId(),
                result.originalFileName(),
                result.storedFileName(),
                result.fileUrl(),
                result.filePath(),
                result.status()
        );
    }
}
