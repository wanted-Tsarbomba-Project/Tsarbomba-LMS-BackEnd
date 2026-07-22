package com.wanted.codebombalms.problems.generation.application.command;

public record GenerateProblemSetDraftAiCommand(
        Long operatorId,
        String question,
        String datasetUrl,
        String dataFileName,
        String topic,
        String categoryName,
        String difficulty,
        int problemCount,
        int subProblemCount
) {
}
