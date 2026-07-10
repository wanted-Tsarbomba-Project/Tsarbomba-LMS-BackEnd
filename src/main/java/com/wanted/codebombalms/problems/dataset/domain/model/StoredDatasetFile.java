package com.wanted.codebombalms.problems.dataset.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;

public class StoredDatasetFile {

    private final String originalFileName;
    private final String storedFileName;
    private final String fileUrl;
    private final String filePath;
    private final Long fileSize;
    private final String metadata;

    private StoredDatasetFile(
            String originalFileName,
            String storedFileName,
            String fileUrl,
            String filePath,
            Long fileSize,
            String metadata
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
        if (fileSize == null || fileSize <= 0) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }

        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.fileUrl = fileUrl;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.metadata = metadata;
    }

    public static StoredDatasetFile create(
            String originalFileName,
            String storedFileName,
            String fileUrl,
            String filePath,
            Long fileSize,
            String metadata
    ) {
        return new StoredDatasetFile(
                originalFileName,
                storedFileName,
                fileUrl,
                filePath,
                fileSize,
                metadata
        );
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

    public Long getFileSize() {
        return fileSize;
    }

    public String getMetadata() {
        return metadata;
    }
}
