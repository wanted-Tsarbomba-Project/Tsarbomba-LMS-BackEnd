package com.wanted.codebombalms.problems.set.application.policy;

import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import org.springframework.stereotype.Component;

@Component
public class ProblemSetDeletionPolicy {

    public void validate(boolean hasSubmission, boolean force) {
        if (hasSubmission && !force) {
            throw new ConflictException(ProblemErrorCode.PROBLEM_HAS_SUBMISSION);
        }
    }
}
