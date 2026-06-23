package com.wanted.codebombalms.problems.set.application.policy;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.command.ProblemCreateCommand;
import com.wanted.codebombalms.problems.set.application.command.ProblemUpdateCommand;
import com.wanted.codebombalms.problems.set.application.command.RegisterProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.command.UpdateProblemSetCommand;
import com.wanted.codebombalms.problems.set.domain.model.ProblemTestCaseModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemTestCaseRegistration;
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
        validateUniqueProblemIds(command);
    }

    private void validateUniqueProblemIds(UpdateProblemSetCommand command) {
        long problemIdCount = command.problems().stream()
                .map(ProblemUpdateCommand::problemId)
                .filter(java.util.Objects::nonNull)
                .count();
        long uniqueProblemIdCount = command.problems().stream()
                .map(ProblemUpdateCommand::problemId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();

        if (problemIdCount != uniqueProblemIdCount) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_INVALID_INPUT);
        }
    }

    private void validate(ProblemCreateCommand command) {
        requireNotBlank(command.title(), ProblemErrorCode.PROBLEM_TITLE_REQUIRED);
        requireNotBlank(command.content(), ProblemErrorCode.PROBLEM_CONTENT_REQUIRED);
        requirePositivePoint(command.point());
        validateTestCases(command.testCases());
    }

    private void validate(ProblemUpdateCommand command) {
        requireNotBlank(command.title(), ProblemErrorCode.PROBLEM_TITLE_REQUIRED);
        requireNotBlank(command.content(), ProblemErrorCode.PROBLEM_CONTENT_REQUIRED);
        requirePositivePoint(command.point());
        validateTestCases(command.testCases());
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

    private void validateTestCases(java.util.List<?> testCases) {
        requireNotEmpty(testCases, ProblemErrorCode.PROBLEM_TEST_CASE_INVALID_INPUT);

        for (Object value : testCases) {
            if (value instanceof ProblemTestCaseRegistration testCase) {
                validateTestCase(testCase.testCode(), testCase.hidden(), testCase.timeoutMs());
            } else if (value instanceof ProblemTestCaseModification testCase) {
                validateTestCase(testCase.testCode(), testCase.hidden(), testCase.timeoutMs());
            } else {
                throw new ValidationException(
                        ProblemErrorCode.PROBLEM_TEST_CASE_INVALID_INPUT
                );
            }
        }
    }

    private void validateTestCase(String testCode, Boolean hidden, Integer timeoutMs) {
        if (testCode == null || testCode.isBlank()
                || hidden == null
                || (timeoutMs != null && (timeoutMs < 1 || timeoutMs > 10_000))) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_TEST_CASE_INVALID_INPUT);
        }
    }
}
