package com.wanted.codebombalms.submission.application.policy;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.submission.exception.SubmissionErrorCode;
import org.springframework.stereotype.Component;

@Component
public class SubmissionAttemptPolicy {

    public void validateAttemptLimit(Integer attemptLimit, Boolean retriable, int previousAttemptCount) {
        if (!Boolean.TRUE.equals(retriable) && previousAttemptCount > 0) {
            throw new ValidationException(SubmissionErrorCode.PROBLEM_NOT_RETRIABLE);
        }
    }

    public Integer calculateRemainingAttemptCount(Integer attemptLimit, int attemptNo) {
        if (attemptLimit == null) {
            return null;
        }

        return Math.max(attemptLimit - attemptNo, 0);
    }

    public boolean canRetry(Boolean isRetriable, Integer remainingAttemptCount, boolean isCorrect) {
        if (isCorrect) {
            return false;
        }

        if (!Boolean.TRUE.equals(isRetriable)) {
            return false;
        }

        return remainingAttemptCount == null || remainingAttemptCount > 0;
    }
}
