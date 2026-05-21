package com.wanted.codebombalms.problems.dataset.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;

public class ProblemDataset {

    private static final String ACTIVE = "ACTIVE";

    private final Long datasetId;
    private final String originalFileName;
    private final String storedFileName;
    private final String fileUrl;
    private final String filePath;
    private final String status;

    private ProblemDataset(
            Long datasetId,
            String originalFileName,
            String storedFileName,
            String fileUrl,
            String filePath,
            String status
    ) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }
        if (storedFileName == null || storedFileName.isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }
        if (filePath == null || filePath.isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }
        if (status == null || status.isBlank()) {
            throw new ValidationException(ProblemErrorCode.INVALID_INPUT);
        }

        this.datasetId = datasetId;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.fileUrl = fileUrl;
        this.filePath = filePath;
        this.status = status;
    }

    public static ProblemDataset createUploaded(
            String originalFileName,
            String storedFileName,
            String fileUrl,
            String filePath
    ) {
        return new ProblemDataset(
                null,
                originalFileName,
                storedFileName,
                fileUrl,
                filePath,
                ACTIVE
        );
    }

    public static ProblemDataset restore(
            Long datasetId,
            String originalFileName,
            String storedFileName,
            String fileUrl,
            String filePath,
            String status
    ) {
        return new ProblemDataset(
                datasetId,
                originalFileName,
                storedFileName,
                fileUrl,
                filePath,
                status
        );
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getStatus() {
        return status;
    }
}
