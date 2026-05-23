package com.wanted.codebombalms.problems.testcase.application.usecase;

import com.wanted.codebombalms.problems.testcase.application.query.GetProblemTestCasesQuery;
import com.wanted.codebombalms.problems.testcase.application.usecase.ProblemTestCaseCommandUseCase.TestCaseView;

import java.util.List;

public interface ProblemTestCaseQueryUseCase {

    List<TestCaseView> handle(GetProblemTestCasesQuery query);
}