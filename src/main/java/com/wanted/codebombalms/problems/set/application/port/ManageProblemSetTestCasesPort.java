package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.domain.model.ProblemTestCaseModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemTestCaseRegistration;

import java.util.List;

public interface ManageProblemSetTestCasesPort {

    int createTestCases(Long problemId, List<ProblemTestCaseRegistration> testCases);

    int synchronizeTestCases(Long problemId, List<ProblemTestCaseModification> testCases);

    int deactivateActiveTestCasesByProblemIds(List<Long> problemIds);
}
