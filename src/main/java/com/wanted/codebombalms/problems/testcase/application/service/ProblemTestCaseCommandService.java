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
        problemTestCasePolicy.validateCreatable(command.problemId(),command.score());

        ProblemTestCase testCase = ProblemTestCase.create(
                command.problemId(),
                command.testCode(),
                command.expectedResult(),
                command.testOrder(),
                command.score(),
                command.hidden(),
                command.timeoutMs()
        );

        return toView(problemTestCaseRepository.save(testCase));
    }

    @Override
    @Transactional
    public ProblemTestCaseCommandUseCase.TestCaseView handle(UpdateProblemTestCaseCommand command) {
        ProblemTestCase existingTestCase = problemTestCaseRepository.findActiveById(command.testCaseId());

        problemTestCasePolicy.validateUpdatable(
                existingTestCase.getProblemId(),
                command.score()
        );

        ProblemTestCase updatedTestCase = ProblemTestCase.restore(
                existingTestCase.getTestCaseId(),
                existingTestCase.getProblemId(),
                command.testCode(),
                command.expectedResult(),
                command.testOrder(),
                command.score(),
                command.hidden(),
                command.timeoutMs(),
                existingTestCase.getStatus()
        );

        return toView(problemTestCaseRepository.save(updatedTestCase));
    }

    @Override
    @Transactional
    public ProblemTestCaseCommandUseCase.TestCaseView delete(Long testCaseId) {
        return toView(problemTestCaseRepository.deactivate(testCaseId));
    }

    private ProblemTestCaseCommandUseCase.TestCaseView toView(ProblemTestCase testCase) {
        return new ProblemTestCaseCommandUseCase.TestCaseView(
                testCase.getTestCaseId(),
                testCase.getProblemId(),
                testCase.getTestCode(),
                testCase.getExpectedResult(),
                testCase.getTestOrder(),
                testCase.getScore(),
                testCase.getHidden(),
                testCase.getTimeoutMs(),
                testCase.getStatus()
        );
    }


}
