package com.wanted.codebombalms.problems.testcase.application.usecase;

import com.wanted.codebombalms.problems.testcase.application.command.CreateProblemTestCaseCommand;
import com.wanted.codebombalms.problems.testcase.application.command.UpdateProblemTestCaseCommand;

public interface ProblemTestCaseCommandUseCase {

    TestCaseView handle(CreateProblemTestCaseCommand command);

    TestCaseView handle(UpdateProblemTestCaseCommand command);

    TestCaseView delete(Long testCaseId);

    record TestCaseView(
            Long testCaseId,
            Long problemId,
            String testCode,
            String expectedResult,
            Integer testOrder,
            Integer score,
            Boolean hidden,
            Integer timeoutMs,
            String status
    ) {
    }
}

