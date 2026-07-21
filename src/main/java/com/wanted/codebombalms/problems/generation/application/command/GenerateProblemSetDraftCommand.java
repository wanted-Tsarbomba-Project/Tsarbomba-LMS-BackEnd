package com.wanted.codebombalms.problems.generation.application.command;

public record GenerateProblemSetDraftCommand(
        Long operatorId,
        String question,
        String datasetUrl,
        String draftToken,
        String datasetObjectName,
        String dataFileName,
        String topic,
        String categoryName,
        String difficulty,
        int problemCount,
        int subProblemCount
) {
}
