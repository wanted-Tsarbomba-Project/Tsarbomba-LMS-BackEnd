package com.wanted.codebombalms.domain.problems.dataset.dto.response;

import com.wanted.codebombalms.domain.problems.dataset.entitiy.ProblemDataset;

public record ProblemDatasetUploadResponse(
        Long datasetId,
        String originalFileName,
        String storedFileName,
        String fileUrl,
        String filePath,
        String status
) {
    public ProblemDatasetUploadResponse(ProblemDataset dataset) {
        this(
                dataset.getDatasetId(),
                dataset.getOriginalFileName(),
                dataset.getStoredFileName(),
                dataset.getFileUrl(),
                dataset.getFilePath(),
                dataset.getStatus()
        );
    }
}
