package com.wanted.codebombalms.problems.dataset.application.policy;

import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.dataset.domain.model.ProblemDatasetConnectionTarget;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ProblemDatasetConnectionPolicy {

    public void validate(ProblemDatasetConnectionTarget target) {
        if (target.isAlreadyConnected()) {
            throw new ConflictException(ProblemErrorCode.PROBLEM_DATASET_ALREADY_CONNECTED);
        }

        if (!target.isCodeProblem()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_PROBLEM_TYPE);
        }
    }
}
