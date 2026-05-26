package com.wanted.codebombalms.problems.testcase.application.service;

import com.wanted.codebombalms.problems.testcase.application.policy.ProblemTestCasePolicy;
import com.wanted.codebombalms.problems.testcase.application.query.GetProblemTestCasesQuery;
import com.wanted.codebombalms.problems.testcase.application.usecase.ProblemTestCaseCommandUseCase;
import com.wanted.codebombalms.problems.testcase.application.usecase.ProblemTestCaseQueryUseCase;
import com.wanted.codebombalms.problems.testcase.domain.model.ProblemTestCase;
import com.wanted.codebombalms.problems.testcase.domain.repository.ProblemTestCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
public class ProblemTestCaseQueryService implements ProblemTestCaseQueryUseCase {

    private final ProblemTestCasePolicy problemTestCasePolicy;
    private final ProblemTestCaseRepository problemTestCaseRepository;

    public ProblemTestCaseQueryService(
            ProblemTestCasePolicy problemTestCasePolicy,
            ProblemTestCaseRepository problemTestCaseRepository
    ) {
        this.problemTestCasePolicy = problemTestCasePolicy;
        this.problemTestCaseRepository = problemTestCaseRepository;
    }

    @Override
    public List<ProblemTestCaseCommandUseCase.TestCaseView> handle(GetProblemTestCasesQuery query) {
        problemTestCasePolicy.validateReadable(query.problemId());

        return problemTestCaseRepository.findActiveByProblemId(query.problemId())
                .stream()
                .map(this::toView)
                .toList();
    }

    private ProblemTestCaseCommandUseCase.TestCaseView toView(ProblemTestCase testCase) {
        return new ProblemTestCaseCommandUseCase.TestCaseView(
                testCase.getTestCaseId(),
                testCase.getProblemId(),
                testCase.getTestCode(),
                testCase.getExpectedResult(),
                testCase.getTestOrder(),
                testCase.getHidden(),
                testCase.getTimeoutMs(),
                testCase.getStatus()
        );
    }
}