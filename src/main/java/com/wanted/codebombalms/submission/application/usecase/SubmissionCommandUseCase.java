package com.wanted.codebombalms.submission.application.usecase;

import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;

public interface SubmissionCommandUseCase {

    SubmissionView handle(Long problemId, SubmitCodeCommand command);

    record SubmissionView(
            Long submissionId,
            Long problemId,
            Boolean correct,
            Integer passedTestCount,
            Integer totalTestCount,
            String executionStatus,
            String errorMessage,
            Integer attemptNo,
            Integer remainingAttemptCount,
            Boolean canRetry,
            Long nextProblemId,
            Boolean problemSetCompleted,
            Integer earnedPoint,
            Boolean pointGranted,
            String explanation

    ) {
    }
}
