package com.wanted.codebombalms.submission.presentation.api.response;

import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase.SubmissionView;

public record SubmissionResponse(
        Long problemId,
        Boolean isCorrect,
        Integer attemptNo,
        Integer remainingAttemptCount,
        Boolean canRetry,
        Long nextProblemId,
        Boolean isProblemSetCompleted,
        String explanation
) {

    public SubmissionResponse(SubmissionView result) {
        this(
                result.problemId(),
                result.correct(),
                result.attemptNo(),
                result.remainingAttemptCount(),
                result.canRetry(),
                result.nextProblemId(),
                result.problemSetCompleted(),
                result.explanation()
        );
    }
}
