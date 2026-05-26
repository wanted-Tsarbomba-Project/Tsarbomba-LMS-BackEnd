package com.wanted.codebombalms.problems.dataset.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;

public class ProblemDatasetConnection {

    private final Long problemId;
    private final Long datasetId;
    private final String fileUrl;

    private ProblemDatasetConnection(Long problemId, Long datasetId, String fileUrl) {
        if (problemId == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_NOT_FOUND);
        }
        if (datasetId == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND);
        }
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }

        this.problemId = problemId;
        this.datasetId = datasetId;
        this.fileUrl = fileUrl;
    }

    public static ProblemDatasetConnection connect(Long problemId, Long datasetId, String filePath) {
        return new ProblemDatasetConnection(problemId, datasetId, filePath);
    }

    public Long getProblemId() {
        return problemId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}
