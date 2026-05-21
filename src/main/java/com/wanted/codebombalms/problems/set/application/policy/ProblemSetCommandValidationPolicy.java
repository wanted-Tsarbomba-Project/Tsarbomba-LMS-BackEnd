package com.wanted.codebombalms.problems.set.application.policy;

import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.command.ProblemCreateCommand;
import com.wanted.codebombalms.problems.set.application.command.ProblemUpdateCommand;
import com.wanted.codebombalms.problems.set.application.command.RegisterProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.command.UpdateProblemSetCommand;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class ProblemSetCommandValidationPolicy {

    public void validate(RegisterProblemSetCommand command) {
        validateProblemSet(command.title(), command.categoryName(), command.problems());
        command.problems().forEach(this::validate);
    }

    public void validate(UpdateProblemSetCommand command) {
        validateProblemSet(command.title(), command.categoryName(), command.problems());
        command.problems().forEach(this::validate);
    }

    private void validate(ProblemCreateCommand command) {
        requireNotBlank(command.title(), ProblemErrorCode.PROBLEM_TITLE_REQUIRED);
        requireNotBlank(command.content(), ProblemErrorCode.PROBLEM_CONTENT_REQUIRED);
        requireNotBlank(command.answer(), ProblemErrorCode.PROBLEM_ANSWER_REQUIRED);
    }

    private void validate(ProblemUpdateCommand command) {
        requireNotBlank(command.title(), ProblemErrorCode.PROBLEM_TITLE_REQUIRED);
        requireNotBlank(command.content(), ProblemErrorCode.PROBLEM_CONTENT_REQUIRED);
        requireNotBlank(command.answer(), ProblemErrorCode.PROBLEM_ANSWER_REQUIRED);
    }

    private void validateProblemSet(String title, String categoryName, java.util.List<?> problems) {
        requireNotBlank(title, ProblemErrorCode.PROBLEM_SET_TITLE_REQUIRED);
        requireNotBlank(categoryName, ProblemErrorCode.PROBLEM_CATEGORY_REQUIRED);
        requireNotEmpty(problems, ProblemErrorCode.PROBLEM_REQUIRED);
    }

    private void requireNotBlank(String value, ProblemErrorCode errorCode) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(errorCode);
        }
    }

    private void requireNotEmpty(java.util.List<?> value, ProblemErrorCode errorCode) {
        if (value == null || value.isEmpty()) {
            throw new ValidationException(errorCode);
        }
    }
}
