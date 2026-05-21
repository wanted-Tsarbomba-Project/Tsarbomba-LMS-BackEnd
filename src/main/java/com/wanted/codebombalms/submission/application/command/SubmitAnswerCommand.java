package com.wanted.codebombalms.submission.application.command;

public record SubmitAnswerCommand(
        Long userId,
        String submittedAnswer
) {
}
