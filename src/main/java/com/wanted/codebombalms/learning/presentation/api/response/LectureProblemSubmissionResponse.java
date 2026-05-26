package com.wanted.codebombalms.learning.presentation.api.response;

import com.wanted.codebombalms.learning.application.usecase.LectureProblemSubmissionUseCase.LectureProblemSubmissionResult;

public record LectureProblemSubmissionResponse(
        Long lectureProblemSubmissionId,
        Long courseProblemStepId,
        Long problemId,
        boolean correct,
        int attemptNo,
        int remainingAttemptCount,
        boolean canRetry,
        boolean completed,
        String explanation
) {

    public static LectureProblemSubmissionResponse from(LectureProblemSubmissionResult result) {
        return new LectureProblemSubmissionResponse(
                result.lectureProblemSubmissionId(),
                result.courseProblemStepId(),
                result.problemId(),
                result.correct(),
                result.attemptNo(),
                result.remainingAttemptCount(),
                result.canRetry(),
                result.completed(),
                result.explanation()
        );
    }
}