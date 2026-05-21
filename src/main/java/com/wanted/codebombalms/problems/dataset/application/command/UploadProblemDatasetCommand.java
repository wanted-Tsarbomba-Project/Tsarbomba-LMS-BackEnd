package com.wanted.codebombalms.problems.dataset.application.command;

public record UploadProblemDatasetCommand(
        String originalFileName,
        byte[] content,
        long fileSize
) {
}
