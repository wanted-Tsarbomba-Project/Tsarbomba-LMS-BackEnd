package com.wanted.codebombalms.domain.problems.dataset.storage;

public record StoredDatasetFile(
        String originalFileName,
        String storedFileName,
        String fileUrl,
        String filePath,
        Long fileSize
) {
}
