package com.wanted.codebombalms.learning.application.usecase;

import com.wanted.codebombalms.learning.application.command.SubmitLectureProblemCommand;

public interface LectureProblemSubmissionUseCase {

    LectureProblemSubmissionResult submit(SubmitLectureProblemCommand command);

    record LectureProblemSubmissionResult(
            Long lectureProblemSubmissionId,
            Long courseProblemStepId,
            Long problemId,
            boolean correct,
            int score,
            int attemptNo,
            int remainingAttemptCount,
            boolean canRetry,
            boolean completed,
            String explanation
    ) {
    }
}
