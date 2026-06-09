package com.wanted.codebombalms.problems.set.application.port;

import java.util.List;

public interface LoadTestCasesForUpdatePort {

    List<TestCaseForUpdateData> loadActiveTestCasesForUpdate(Long problemId);

    record TestCaseForUpdateData(
            Long testCaseId,
            String testCode,
            Boolean hidden,
            Integer timeoutMs
    ) {
    }
}
