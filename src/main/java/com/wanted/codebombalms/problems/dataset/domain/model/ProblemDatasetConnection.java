package com.wanted.codebombalms.problems.dataset.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;

public class ProblemDatasetConnection {

    private final Long problemSetId;
    private final Long datasetId;
    private final String fileUrl;

    private ProblemDatasetConnection(Long problemSetId, Long datasetId, String fileUrl) {
        if (problemSetId == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND);
        }
        if (datasetId == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND);
        }
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }

        this.problemSetId = problemSetId;
        this.datasetId = datasetId;
        this.fileUrl = fileUrl;
    }

    public static ProblemDatasetConnection connect(Long problemSetId, Long datasetId, String fileUrl) {
        return new ProblemDatasetConnection(problemSetId, datasetId, fileUrl);
    }

    public Long getProblemSetId() {
        return problemSetId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}
