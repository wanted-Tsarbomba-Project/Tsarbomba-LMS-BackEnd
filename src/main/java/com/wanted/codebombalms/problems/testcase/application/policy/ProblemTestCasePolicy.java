package com.wanted.codebombalms.problems.testcase.application.policy;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.testcase.application.port.LoadTestCaseProblemPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemTestCasePolicy {

    private static final String CODE_PROBLEM_TYPE = "CODE";

    private final LoadTestCaseProblemPort loadTestCaseProblemPort;


    public void validateCreatable(Long problemId) {
        validateCodeProblem(problemId);
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
}
