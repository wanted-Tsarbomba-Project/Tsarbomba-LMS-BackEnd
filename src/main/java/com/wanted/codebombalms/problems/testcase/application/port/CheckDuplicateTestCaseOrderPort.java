package com.wanted.codebombalms.problems.testcase.application.port;

public interface CheckDuplicateTestCaseOrderPort {

    boolean existsActiveOrder(Long problemId, Integer testOrder);

    boolean existsActiveOrderExceptSelf(Long problemId, Integer testOrder, Long testCaseId);
}
