package com.wanted.codebombalms.problems.testcase.domain.repository;

import com.wanted.codebombalms.problems.testcase.domain.model.ProblemTestCase;

import java.util.List;

public interface ProblemTestCaseRepository {
    ProblemTestCase save(ProblemTestCase testCase);

    List<ProblemTestCase> findActiveByProblemId(Long problemId);

    ProblemTestCase findActiveById(Long testCaseId);

    ProblemTestCase deactivate(Long testCaseId);
}

