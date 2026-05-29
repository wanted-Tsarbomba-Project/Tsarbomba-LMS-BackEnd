package com.wanted.codebombalms.problems.testcase.application.policy;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.testcase.application.port.LoadTestCaseProblemPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.wanted.codebombalms.problems.testcase.application.port.CheckDuplicateTestCaseOrderPort;

@Component
@RequiredArgsConstructor
public class ProblemTestCasePolicy {

    private static final String CODE_PROBLEM_TYPE = "CODE";

    private final LoadTestCaseProblemPort loadTestCaseProblemPort;

    private final CheckDuplicateTestCaseOrderPort checkDuplicateTestCaseOrderPort;

    public void validateCreatable(Long problemId, Integer testOrder) {
        validateCodeProblem(problemId);
        validateDuplicateOrder(problemId, testOrder);
    }

    private void validateDuplicateOrder(Long problemId, Integer testOrder) {
        if (checkDuplicateTestCaseOrderPort.existsActiveOrder(problemId, testOrder)) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_TEST_CASE_DUPLICATE_ORDER);
        }
    }

    public void validateReadable(Long problemId) {
        validateCodeProblem(problemId);
    }

    public void validateUpdatable(Long problemId) {
        validateCodeProblem(problemId);
    }

    private void validateCodeProblem(Long problemId) {
        var problem = loadTestCaseProblemPort.loadByProblemId(problemId);

        if (!CODE_PROBLEM_TYPE.equals(problem.problemType())) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_TEST_CASE_INVALID_PROBLEM_TYPE);
        }
    }

    public void validateUpdatable(Long problemId, Long testCaseId, Integer testOrder) {
        validateCodeProblem(problemId);
        validateDuplicateOrderExceptSelf(problemId, testCaseId, testOrder);
    }

    private void validateDuplicateOrderExceptSelf(Long problemId, Long testCaseId, Integer testOrder) {
        if (checkDuplicateTestCaseOrderPort.existsActiveOrderExceptSelf(problemId, testOrder, testCaseId)) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_TEST_CASE_DUPLICATE_ORDER);
        }
    }
}
