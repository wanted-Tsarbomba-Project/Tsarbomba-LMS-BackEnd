package com.wanted.codebombalms.submission.application.port;

import java.util.List;

public interface LoadTestCasesForGradingPort {

    List<TestCaseForGrading> loadActiveTestCases(Long problemId);

    record TestCaseForGrading(
            Long testCaseId,
            String testCode,
            String expectedResult,
            Integer score,
            Boolean hidden,
            Integer timeoutMs
    ) {
    }
}
