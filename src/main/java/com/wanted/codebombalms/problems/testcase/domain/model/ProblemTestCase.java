package com.wanted.codebombalms.problems.testcase.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;

public class ProblemTestCase {

    private static final String ACTIVE = "ACTIVE";
    private static final int DEFAULT_TIMEOUT_MS = 3000;

    private final Long testCaseId;
    private final Long problemId;
    private final String testCode;
    private final Integer testOrder;
    private final Boolean hidden;
    private final Integer timeoutMs;
    private final String status;

    private ProblemTestCase(
            Long testCaseId,
            Long problemId,
            String testCode,
            Integer testOrder,
            Boolean hidden,
            Integer timeoutMs,
            String status
    ) {
        validate(problemId, testCode, testOrder, hidden, status);

        this.testCaseId = testCaseId;
        this.problemId = problemId;
        this.testCode = testCode;
        this.testOrder = testOrder;
        this.hidden = hidden;
        this.timeoutMs = timeoutMs == null ? DEFAULT_TIMEOUT_MS : timeoutMs;
        this.status = status;
    }

    public static ProblemTestCase create(
            Long problemId,
            String testCode,
            Integer testOrder,
            Boolean hidden,
            Integer timeoutMs
    ) {
        return new ProblemTestCase(
                null,
                problemId,
                testCode,
                testOrder,
                hidden,
                timeoutMs,
                ACTIVE
        );
    }

    public static ProblemTestCase restore(
            Long testCaseId,
            Long problemId,
            String testCode,
            Integer testOrder,
            Boolean hidden,
            Integer timeoutMs,
            String status
    ) {
        return new ProblemTestCase(
                testCaseId,
                problemId,
                testCode,
                testOrder,
                hidden,
                timeoutMs,
                status
        );
    }

    private void validate(
            Long problemId,
            String testCode,
            Integer testOrder,
            Boolean hidden,
            String status
    ) {
        if (problemId == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_NOT_FOUND);
        }
        if (testCode == null || testCode.isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_TEST_CASE_INVALID_INPUT);
        }
        if (testOrder == null || testOrder < 1) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_TEST_CASE_INVALID_INPUT);
        }
        if (hidden == null) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_TEST_CASE_INVALID_INPUT);
        }
        if (status == null || status.isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_TEST_CASE_INVALID_INPUT);
        }
    }

    public Long getTestCaseId() {
        return testCaseId;
    }

    public Long getProblemId() {
        return problemId;
    }

    public String getTestCode() {
        return testCode;
    }

    public Integer getTestOrder() {
        return testOrder;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public String getStatus() {
        return status;
    }
}
