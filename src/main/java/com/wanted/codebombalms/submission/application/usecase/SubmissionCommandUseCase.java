package com.wanted.codebombalms.submission.application.usecase;

import com.wanted.codebombalms.submission.application.command.SubmitAnswerCommand;

public interface SubmissionCommandUseCase {

    SubmissionView handle(Long problemId, SubmitAnswerCommand command);

    record SubmissionView(
            Long problemId,
            Boolean correct,
            Integer attemptNo,
            Integer remainingAttemptCount,
            Boolean canRetry,
            Long nextProblemId,
            Boolean problemSetCompleted,
            String explanation
    ) {
    }
}
