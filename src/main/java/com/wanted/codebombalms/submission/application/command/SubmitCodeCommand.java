package com.wanted.codebombalms.submission.application.command;

public record SubmitCodeCommand(
        Long userId,
        String code
) {
}
