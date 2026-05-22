package com.wanted.codebombalms.submission.presentation.api.response;

import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase.SubmissionView;

public record SubmissionResponse(
        Long submissionId,
        Long problemId,
        Boolean isCorrect,
        Integer earnedScore,
        Integer passedTestCount,
        Integer totalTestCount,
        String executionStatus,
        String errorMessage,
        Integer attemptNo,
        Integer remainingAttemptCount,
        Boolean canRetry,
        Long nextProblemId,
        Boolean isProblemSetCompleted,
        String explanation
) {

    public SubmissionResponse(SubmissionView result) {
        this(
                result.submissionId(),
                result.problemId(),
                result.correct(),
                result.earnedScore(),
                result.passedTestCount(),
                result.totalTestCount(),
                result.executionStatus(),
                result.errorMessage(),
                result.attemptNo(),
                result.remainingAttemptCount(),
                result.canRetry(),
                result.nextProblemId(),
                result.problemSetCompleted(),
                result.explanation()
        );
    }
}
