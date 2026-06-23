package com.wanted.codebombalms.submission.infrastructure.testcase;

import com.wanted.codebombalms.problems.testcase.domain.model.ProblemTestCase;
import com.wanted.codebombalms.problems.testcase.domain.repository.ProblemTestCaseRepository;
import com.wanted.codebombalms.submission.application.port.LoadTestCasesForGradingPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestCaseForGradingAdapter implements LoadTestCasesForGradingPort {

    private final ProblemTestCaseRepository problemTestCaseRepository;

    public TestCaseForGradingAdapter(ProblemTestCaseRepository problemTestCaseRepository) {
        this.problemTestCaseRepository = problemTestCaseRepository;
    }

    @Override
    public List<TestCaseForGrading> loadActiveTestCases(Long problemId) {
        return problemTestCaseRepository.findActiveByProblemId(problemId)
                .stream()
                .map(this::toTestCaseForGrading)
                .toList();
    }

    private TestCaseForGrading toTestCaseForGrading(ProblemTestCase testCase) {
        return new TestCaseForGrading(
                testCase.getTestCaseId(),
                testCase.getTestCode(),
                testCase.getHidden(),
                testCase.getTimeoutMs()
        );
    }
}
