package com.wanted.codebombalms.problems.dataset.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;

public class ProblemDatasetConnectionRequest {

    private final Long problemId;
    private final Long datasetId;

    private ProblemDatasetConnectionRequest(Long problemId, Long datasetId) {
        if (problemId == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_NOT_FOUND);
        }
        if (datasetId == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND);
        }

        this.problemId = problemId;
        this.datasetId = datasetId;
    }

    public static ProblemDatasetConnectionRequest of(Long problemId, Long datasetId) {
        return new ProblemDatasetConnectionRequest(problemId, datasetId);
    }

    public Long getProblemId() {
        return problemId;
    }

    public Long getDatasetId() {
        return datasetId;
    }
}
