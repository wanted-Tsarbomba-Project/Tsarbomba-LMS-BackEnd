package com.wanted.codebombalms.problems.generation.domain;

public record StoredProblemSetDraftDataset(
        String originalFileName,
        String storedFileName,
        String objectName,
        long fileSize
) {
}
