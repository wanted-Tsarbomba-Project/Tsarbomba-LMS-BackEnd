package com.wanted.codebombalms.problems.generation.application.command;

import java.io.InputStream;

public record GenerateProblemSetDraftCommand(
        Long operatorId,
        String question,
        String dataFileName,
        String topic,
        String categoryName,
        String difficulty,
        int problemCount,
        int subProblemCount,
        String originalFileName,
        String contentType,
        InputStream datasetContent,
        long datasetFileSize
) {
}
