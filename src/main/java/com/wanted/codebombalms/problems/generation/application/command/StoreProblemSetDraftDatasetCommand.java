package com.wanted.codebombalms.problems.generation.application.command;

public record StoreProblemSetDraftDatasetCommand(
        Long operatorId,
        String draftToken,
        String originalFileName,
        String contentType,
        byte[] content,
        long fileSize
) {
}
