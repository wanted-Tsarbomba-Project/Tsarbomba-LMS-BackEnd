package com.wanted.codebombalms.problems.execution.application.policy;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import org.springframework.stereotype.Component;

@Component
public class CodeExecutionPolicy {

    public void validate(String code) {
        if (code == null || code.isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_CODE_INVALID_INPUT);
        }
    }

    public void validateDatasetAccess(String datasetPath) {
        if (datasetPath == null || datasetPath.isBlank()) {
            throw new NotFoundException(ProblemErrorCode.PROBLEM_DATASET_NOT_FOUND);
        }
    }
}
