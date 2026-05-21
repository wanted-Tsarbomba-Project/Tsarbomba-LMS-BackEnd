package com.wanted.codebombalms.submission.application.policy;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.submission.exception.SubmissionErrorCode;
import org.springframework.stereotype.Component;

@Component
public class SubmissionAttemptPolicy {

    public void validateAttemptLimit(Integer attemptLimit, Boolean retriable, int previousAttemptCount) {
        if (attemptLimit != null && previousAttemptCount >= attemptLimit) {
            throw new ValidationException(SubmissionErrorCode.ATTEMPT_LIMIT_EXCEEDED);
        }

        if (!Boolean.TRUE.equals(retriable) && previousAttemptCount > 0) {
            throw new ValidationException(SubmissionErrorCode.PROBLEM_NOT_RETRIABLE);
        }
    }

    public int calculateRemainingAttemptCount(Integer attemptLimit, int attemptNo) {
        if (attemptLimit == null) {
            return 0;
        }

        return Math.max(attemptLimit - attemptNo, 0);
    }

    public boolean canRetry(Boolean isRetriable, int remainingAttemptCount, boolean isCorrect) {
        return !isCorrect
                && Boolean.TRUE.equals(isRetriable)
                && remainingAttemptCount > 0;
    }
}