package com.wanted.codebombalms.problems.set.application.policy;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.command.ProblemCreateCommand;
import com.wanted.codebombalms.problems.set.application.command.ProblemUpdateCommand;
import com.wanted.codebombalms.problems.set.application.command.RegisterProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.command.UpdateProblemSetCommand;
import org.springframework.stereotype.Component;

@Component
public class ProblemSetCommandValidationPolicy {

    public void validate(RegisterProblemSetCommand command) {
        if (command.createdBy() == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_INVALID_INPUT);
        }

        validateProblemSet(
                command.title(),
                command.categoryName(),
                command.difficulty(),
                command.problems()
        );
        command.problems().forEach(this::validate);
    }

    public void validate(UpdateProblemSetCommand command) {
        validateProblemSet(command.title(), command.categoryName(), command.difficulty(), command.problems());
        command.problems().forEach(this::validate);
    }

    private void validate(ProblemCreateCommand command) {
        requireNotBlank(command.title(), ProblemErrorCode.PROBLEM_TITLE_REQUIRED);
        requireNotBlank(command.content(), ProblemErrorCode.PROBLEM_CONTENT_REQUIRED);
        requireNotBlank(command.answer(), ProblemErrorCode.PROBLEM_ANSWER_REQUIRED);
        requirePositivePoint(command.point());
    }

    private void validate(ProblemUpdateCommand command) {
        requireNotBlank(command.title(), ProblemErrorCode.PROBLEM_TITLE_REQUIRED);
        requireNotBlank(command.content(), ProblemErrorCode.PROBLEM_CONTENT_REQUIRED);
        requireNotBlank(command.answer(), ProblemErrorCode.PROBLEM_ANSWER_REQUIRED);
        requirePositivePoint(command.point());
    }

    private void validateProblemSet(
            String title,
            String categoryName,
            String difficulty,
            java.util.List<?> problems
    ) {
        requireNotBlank(title, ProblemErrorCode.PROBLEM_SET_TITLE_REQUIRED);
        requireNotBlank(categoryName, ProblemErrorCode.PROBLEM_CATEGORY_REQUIRED);
        validateDifficulty(difficulty);
        requireNotEmpty(problems, ProblemErrorCode.PROBLEM_REQUIRED);
    }

    private void validateDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_INVALID_INPUT);
        }

        if (!difficulty.equals("EASY")
                && !difficulty.equals("MEDIUM")
                && !difficulty.equals("HARD")) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_INVALID_INPUT);
        }
    }

    private void requirePositivePoint(Integer point) {
        if (point == null || point <= 0) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_POINT_REQUIRED);
        }
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
