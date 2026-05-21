package com.wanted.codebombalms.problems.dataset.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;

public class ProblemDatasetConnection {

    private final Long problemId;
    private final Long datasetId;
    private final String filePath;

    private ProblemDatasetConnection(Long problemId, Long datasetId, String filePath) {
        if (problemId == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_NOT_FOUND);
        }
        if (datasetId == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND);
        }
        if (filePath == null || filePath.isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }

        this.problemId = problemId;
        this.datasetId = datasetId;
        this.filePath = filePath;
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

    public String getFilePath() {
        return filePath;
    }
}
