package com.wanted.codebombalms.problems.dataset.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;

public record ProblemDatasetConnectionTarget(
        Long problemSetId,
        Long datasetId,
        Long connectedProblemSetId,
        String fileUrl
) {

    public void validateConnectable() {
        if (isAlreadyConnected()) {
            throw new ConflictException(ProblemErrorCode.PROBLEM_DATASET_ALREADY_CONNECTED);
        }
    }

    public boolean isAlreadyConnected() {
        return connectedProblemSetId != null;
    }
}