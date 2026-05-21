package com.wanted.codebombalms.submission.application.service;

import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;
import com.wanted.codebombalms.domain.submission.exception.SubmissionErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class SubmissionAttemptService {

    public void validateAttemptLimit(Problem problem, int previousAttemptCount) {
        if (problem.getAttemptLimit() != null && previousAttemptCount >= problem.getAttemptLimit()) {
            throw new ValidationException(SubmissionErrorCode.ATTEMPT_LIMIT_EXCEEDED);
        }

        if (!Boolean.TRUE.equals(problem.getRetriable()) && previousAttemptCount > 0) {
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
