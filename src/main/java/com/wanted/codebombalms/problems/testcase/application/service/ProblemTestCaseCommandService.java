package com.wanted.codebombalms.problems.testcase.application.service;

import com.wanted.codebombalms.problems.testcase.application.command.CreateProblemTestCaseCommand;
import com.wanted.codebombalms.problems.testcase.application.command.UpdateProblemTestCaseCommand;
import com.wanted.codebombalms.problems.testcase.application.policy.ProblemTestCasePolicy;
import com.wanted.codebombalms.problems.testcase.application.usecase.ProblemTestCaseCommandUseCase;
import com.wanted.codebombalms.problems.testcase.domain.model.ProblemTestCase;
import com.wanted.codebombalms.problems.testcase.domain.repository.ProblemTestCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProblemTestCaseCommandService implements ProblemTestCaseCommandUseCase {

    private final ProblemTestCasePolicy problemTestCasePolicy;
    private final ProblemTestCaseRepository problemTestCaseRepository;

    public ProblemTestCaseCommandService(
            ProblemTestCasePolicy problemTestCasePolicy,
            ProblemTestCaseRepository problemTestCaseRepository
    ) {
        this.problemTestCasePolicy = problemTestCasePolicy;
        this.problemTestCaseRepository = problemTestCaseRepository;
    }

    @Override
    @Transactional
    public TestCaseView handle(CreateProblemTestCaseCommand command) {
        problemTestCasePolicy.validateCreatable(
                command.problemId(),
                command.testOrder()
        );

        ProblemTestCase testCase = ProblemTestCase.create(
                command.problemId(),
                command.testCode(),
                command.expectedResult(),
                command.testOrder(),
                command.hidden(),
                command.timeoutMs()
        );

        return toView(problemTestCaseRepository.save(testCase));
    }

    @Override
    @Transactional
    public TestCaseView handle(UpdateProblemTestCaseCommand command) {
        ProblemTestCase existingTestCase = problemTestCaseRepository.findActiveById(command.testCaseId());

        problemTestCasePolicy.validateUpdatable(
                existingTestCase.getProblemId(),
                existingTestCase.getTestCaseId(),
                command.testOrder()
        );

        ProblemTestCase updatedTestCase = ProblemTestCase.restore(
                existingTestCase.getTestCaseId(),
                existingTestCase.getProblemId(),
                command.testCode(),
                command.expectedResult(),
                command.testOrder(),
                command.hidden(),
                command.timeoutMs(),
                existingTestCase.getStatus()
        );

        return toView(problemTestCaseRepository.save(updatedTestCase));
    }

    @Override
    @Transactional
    public TestCaseView delete(Long testCaseId) {
        return toView(problemTestCaseRepository.deactivate(testCaseId));
    }

    private TestCaseView toView(ProblemTestCase testCase) {
        return new TestCaseView(
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
