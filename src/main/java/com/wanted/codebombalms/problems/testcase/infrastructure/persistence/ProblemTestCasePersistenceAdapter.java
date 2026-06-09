package com.wanted.codebombalms.problems.testcase.infrastructure.persistence;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.set.application.port.ManageProblemSetTestCasesPort;
import com.wanted.codebombalms.problems.set.application.port.LoadTestCasesForUpdatePort;
import com.wanted.codebombalms.problems.set.domain.model.ProblemTestCaseModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemTestCaseRegistration;
import com.wanted.codebombalms.problems.testcase.application.port.CheckDuplicateTestCaseOrderPort;
import com.wanted.codebombalms.problems.testcase.domain.model.ProblemTestCase;
import com.wanted.codebombalms.problems.testcase.domain.repository.ProblemTestCaseRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProblemTestCasePersistenceAdapter implements
        ProblemTestCaseRepository,
        CheckDuplicateTestCaseOrderPort,
        LoadTestCasesForUpdatePort,
        ManageProblemSetTestCasesPort {

    private static final String ACTIVE = "ACTIVE";

    private final SpringDataProblemTestCaseRepository testCaseRepository;
    private final EntityManager entityManager;

    @Override
    public ProblemTestCase save(ProblemTestCase testCase) {
        if (testCase.getTestCaseId() == null) {
            ProblemJpaEntity problem = entityManager.getReference(
                    ProblemJpaEntity.class,
                    testCase.getProblemId()
            );

            return toDomain(testCaseRepository.save(ProblemTestCaseJpaEntity.create(
                    problem,
                    testCase.getTestCode(),
                    testCase.getTestOrder(),
                    testCase.getHidden(),
                    testCase.getTimeoutMs()
            )));
        }

        ProblemTestCaseJpaEntity entity = loadActiveTestCase(testCase.getTestCaseId());
        entity.update(
                testCase.getTestCode(),
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
        return testCaseRepository.existsByProblem_ProblemIdAndTestOrderAndStatusAndTestCaseIdNot(
                problemId,
                testOrder,
                ACTIVE,
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
                ACTIVE
        );
    }

    @Override
    public ProblemTestCase deactivate(Long testCaseId) {
        ProblemTestCaseJpaEntity testCase = loadActiveTestCase(testCaseId);
        testCase.deactivate();

        return toDomain(testCase);
    }

    @Override
    public int createTestCases(Long problemId, List<ProblemTestCaseRegistration> testCases) {
        ProblemJpaEntity problem = entityManager.getReference(ProblemJpaEntity.class, problemId);

        for (int index = 0; index < testCases.size(); index++) {
            ProblemTestCaseRegistration testCase = testCases.get(index);
            testCaseRepository.save(ProblemTestCaseJpaEntity.create(
                    problem,
                    testCase.testCode(),
                    index + 1,
                    testCase.hidden(),
                    resolveTimeout(testCase.timeoutMs())
            ));
        }

        return testCases.size();
    }

    @Override
    public Map<Long, List<TestCaseForUpdateData>> loadActiveTestCasesForUpdate(
            List<Long> problemIds
    ) {
        if (problemIds == null || problemIds.isEmpty()) {
            return Map.of();
        }

        return testCaseRepository
                .findByProblem_ProblemIdInAndStatusOrderByProblem_ProblemIdAscTestOrderAsc(
                        problemIds,
                        ACTIVE
                )
                .stream()
                .map(testCase -> new TestCaseForUpdateData(
                        testCase.getProblem().getProblemId(),
                        testCase.getTestCaseId(),
                        testCase.getTestCode(),
                        testCase.getHidden(),
                        testCase.getTimeoutMs()
                ))
                .collect(Collectors.groupingBy(TestCaseForUpdateData::problemId));
    }

    @Override
    public int synchronizeTestCases(Long problemId, List<ProblemTestCaseModification> testCases) {
        List<ProblemTestCaseJpaEntity> existingTestCases =
                testCaseRepository.findByProblem_ProblemIdAndStatusOrderByTestOrderAsc(problemId, ACTIVE);

        Map<Long, ProblemTestCaseJpaEntity> existingById = existingTestCases.stream()
                .collect(Collectors.toMap(ProblemTestCaseJpaEntity::getTestCaseId, Function.identity()));

        Set<Long> requestedIds = testCases.stream()
                .map(ProblemTestCaseModification::testCaseId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        long requestedIdCount = testCases.stream()
                .map(ProblemTestCaseModification::testCaseId)
                .filter(java.util.Objects::nonNull)
                .count();

        if (requestedIdCount != requestedIds.size()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_TEST_CASE_INVALID_INPUT);
        }

        if (!existingById.keySet().containsAll(requestedIds)) {
            throw new NotFoundException(ProblemErrorCode.PROBLEM_TEST_CASE_NOT_FOUND);
        }

        existingTestCases.forEach(testCase ->
                testCase.moveToOrder(-testCase.getTestCaseId().intValue()));
        testCaseRepository.flush();

        ProblemJpaEntity problem = entityManager.getReference(ProblemJpaEntity.class, problemId);

        for (int index = 0; index < testCases.size(); index++) {
            ProblemTestCaseModification testCase = testCases.get(index);
            int testOrder = index + 1;

            if (testCase.testCaseId() == null) {
                testCaseRepository.save(ProblemTestCaseJpaEntity.create(
                        problem,
                        testCase.testCode(),
                        testOrder,
                        testCase.hidden(),
                        resolveTimeout(testCase.timeoutMs())
                ));
                continue;
            }

            existingById.get(testCase.testCaseId()).update(
                    testCase.testCode(),
                    testOrder,
                    testCase.hidden(),
                    resolveTimeout(testCase.timeoutMs())
            );
        }

        existingTestCases.stream()
                .filter(testCase -> !requestedIds.contains(testCase.getTestCaseId()))
                .forEach(ProblemTestCaseJpaEntity::deactivate);

        return testCases.size();
    }

    @Override
    public int deactivateActiveTestCasesByProblemIds(List<Long> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            return 0;
        }

        List<ProblemTestCaseJpaEntity> testCases =
                testCaseRepository.findByProblem_ProblemIdInAndStatus(problemIds, ACTIVE);
        testCases.forEach(ProblemTestCaseJpaEntity::deactivate);

        return testCases.size();
    }

    private int resolveTimeout(Integer timeoutMs) {
        return timeoutMs == null ? 3000 : timeoutMs;
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
                entity.getTestOrder(),
                entity.getHidden(),
                entity.getTimeoutMs(),
                entity.getStatus()
        );
    }
}
