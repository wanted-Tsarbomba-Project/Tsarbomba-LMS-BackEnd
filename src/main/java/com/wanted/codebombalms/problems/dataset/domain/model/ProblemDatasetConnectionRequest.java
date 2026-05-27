package com.wanted.codebombalms.problems.dataset.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;

public class ProblemDatasetConnectionRequest {

    private final Long problemSetId;
    private final Long datasetId;

    private ProblemDatasetConnectionRequest(Long problemSetId, Long datasetId) {
        if (problemSetId == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND);
        }
        if (datasetId == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND);
        }

        this.problemSetId = problemSetId;
        this.datasetId = datasetId;
    }

    public static ProblemDatasetConnectionRequest of(Long problemSetId, Long datasetId) {
        return new ProblemDatasetConnectionRequest(problemSetId, datasetId);
    }

    public Long getProblemSetId() {
        return problemSetId;
    }

    public Long getDatasetId() {
        return datasetId;
    }
}