package com.wanted.codebombalms.problems.testcase.infrastructure.persistence;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.problems.testcase.application.port.CheckDuplicateTestCaseOrderPort;
import com.wanted.codebombalms.problems.testcase.application.port.LoadTestCaseProblemPort;
import com.wanted.codebombalms.problems.testcase.domain.model.ProblemTestCase;
import com.wanted.codebombalms.problems.testcase.domain.repository.ProblemTestCaseRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProblemTestCasePersistenceAdapter implements ProblemTestCaseRepository, CheckDuplicateTestCaseOrderPort,LoadTestCaseProblemPort {

    private static final String ACTIVE = "ACTIVE";

    private final SpringDataProblemTestCaseRepository testCaseRepository;
    private final SpringDataProblemRepository problemRepository;

    public ProblemTestCasePersistenceAdapter(
            SpringDataProblemTestCaseRepository testCaseRepository,
            SpringDataProblemRepository problemRepository
    ) {
        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
    }

    @Override
    public ProblemTestCase save(ProblemTestCase testCase) {
        if (testCase.getTestCaseId() == null) {
            ProblemJpaEntity problem = loadProblem(testCase.getProblemId());

            return toDomain(testCaseRepository.save(ProblemTestCaseJpaEntity.create(
                    problem,
                    testCase.getTestCode(),
                    testCase.getExpectedResult(),
                    testCase.getTestOrder(),
                    testCase.getHidden(),
                    testCase.getTimeoutMs()
            )));
        }

        ProblemTestCaseJpaEntity entity = loadActiveTestCase(testCase.getTestCaseId());
        entity.update(
                testCase.getTestCode(),
                testCase.getExpectedResult(),
                testCase.getTestOrder(),
                testCase.getHidden(),
                testCase.getTimeoutMs()
        );

        return toDomain(entity);
    }

    @Override
    public List<ProblemTestCase> findActiveByProblemId(Long problemId) {
        return testCaseRepository.findByProblem_ProblemIdAndStatusOrderByTestOrderAsc(problemId, ACTIVE)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveOrderExceptSelf(Long problemId, Integer testOrder, Long testCaseId) {
        return testCaseRepository
                .existsByProblem_ProblemIdAndTestOrderAndStatusAndTestCaseIdNot(
                        problemId,
                        testOrder,
                        "ACTIVE",
                        testCaseId
                );
    }

    @Override
    public ProblemTestCase findActiveById(Long testCaseId) {
        return toDomain(loadActiveTestCase(testCaseId));
    }

    @Override
    public boolean existsActiveOrder(Long problemId, Integer testOrder) {
        return testCaseRepository.existsByProblem_ProblemIdAndTestOrderAndStatus(
                problemId,
                testOrder,
                "ACTIVE"
        );
    }

    @Override
    public ProblemTestCase deactivate(Long testCaseId) {
        ProblemTestCaseJpaEntity testCase = loadActiveTestCase(testCaseId);
        testCase.deactivate();

        return toDomain(testCase);
    }

    @Override
    public LoadTestCaseProblemPort.TestCaseProblemView loadByProblemId(Long problemId) {
        ProblemJpaEntity problem = loadProblem(problemId);

        return new LoadTestCaseProblemPort.TestCaseProblemView(
                problem.getProblemId(),
                problem.getProblemType()
        );
    }

    private ProblemJpaEntity loadProblem(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));
    }

    private ProblemTestCaseJpaEntity loadActiveTestCase(Long testCaseId) {
        return testCaseRepository.findByTestCaseIdAndStatus(testCaseId, ACTIVE)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_TEST_CASE_NOT_FOUND));
    }

    private ProblemTestCase toDomain(ProblemTestCaseJpaEntity entity) {
        return ProblemTestCase.restore(
                entity.getTestCaseId(),
                entity.getProblem().getProblemId(),
                entity.getTestCode(),
                entity.getExpectedResult(),
                entity.getTestOrder(),
                entity.getHidden(),
                entity.getTimeoutMs(),
                entity.getStatus()
        );
    }
}

