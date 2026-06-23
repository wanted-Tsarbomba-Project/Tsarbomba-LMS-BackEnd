package com.wanted.codebombalms.problems.set.application.port;

import java.util.List;
import java.util.Map;

public interface LoadTestCasesForUpdatePort {

    Map<Long, List<TestCaseForUpdateData>> loadActiveTestCasesForUpdate(
            List<Long> problemIds
    );

    record TestCaseForUpdateData(
            Long problemId,
            Long testCaseId,
            String testCode,
            Boolean hidden,
            Integer timeoutMs
    ) {
    }
}
