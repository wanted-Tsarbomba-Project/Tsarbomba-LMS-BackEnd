package com.wanted.codebombalms.problems.dataset.application.command;

public record UploadProblemDatasetCommand(
        String originalFileName,
        String contentType,
        byte[] content,
        long fileSize
) {
}
