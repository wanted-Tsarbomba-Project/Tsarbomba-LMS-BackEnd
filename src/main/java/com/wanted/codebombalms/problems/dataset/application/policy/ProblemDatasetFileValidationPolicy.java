package com.wanted.codebombalms.problems.dataset.application.policy;

import com.wanted.codebombalms.problems.dataset.application.command.UploadProblemDatasetCommand;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class ProblemDatasetFileValidationPolicy {

    public void validate(UploadProblemDatasetCommand command) {
        if (command == null || command.content() == null || command.content().length == 0) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }

        String originalFileName = command.originalFileName();

        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".csv")) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_DATASET_INVALID_FILE);
        }
    }
}
