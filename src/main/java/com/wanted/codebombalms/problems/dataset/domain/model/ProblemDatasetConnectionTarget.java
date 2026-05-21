package com.wanted.codebombalms.problems.dataset.domain.model;

public record ProblemDatasetConnectionTarget(
        Long problemId,
        String problemType,
        Long datasetId,
        Long connectedProblemId,
        String filePath
) {

    public boolean isAlreadyConnected() {
        return connectedProblemId != null;
    }

    public boolean isCodeProblem() {
        return "CODE".equals(problemType);
    }
}
