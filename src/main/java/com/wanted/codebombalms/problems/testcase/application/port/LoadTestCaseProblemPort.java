package com.wanted.codebombalms.problems.testcase.application.port;

public interface LoadTestCaseProblemPort {

    TestCaseProblemView loadByProblemId(Long problemId);

    record TestCaseProblemView(
            Long problemId,
            String problemType,
            Integer score
    ) {
    }
}
