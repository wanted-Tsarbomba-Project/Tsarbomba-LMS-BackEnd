package com.wanted.codebombalms.learning.application.command;

public record SubmitLectureProblemCommand(
        Long userId,
        Long courseProblemStepId,
        String submittedAnswer
) {
}
