package com.wanted.codebombalms.problems.generation.application.command;

import java.io.InputStream;

public record StoreProblemSetDraftDatasetCommand(
        Long operatorId,
        String draftToken,
        String originalFileName,
        String contentType,
        InputStream content,
        long fileSize
) {
}
