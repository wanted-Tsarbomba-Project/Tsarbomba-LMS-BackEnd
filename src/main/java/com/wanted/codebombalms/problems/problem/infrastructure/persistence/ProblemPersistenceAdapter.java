package com.wanted.codebombalms.problems.problem.infrastructure.persistence;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.execution.application.port.LoadExecutionProblemPort;
import com.wanted.codebombalms.problems.problem.application.port.LoadProblemSetIdByProblemIdPort;
import com.wanted.codebombalms.problems.problem.domain.model.Problem;
import com.wanted.codebombalms.problems.problem.domain.repository.ProblemRepository;
import com.wanted.codebombalms.problems.testcase.application.port.LoadTestCaseProblemPort;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProblemPersistenceAdapter implements
        ProblemRepository,
        LoadTestCaseProblemPort,
        LoadExecutionProblemPort,
        LoadProblemSetIdByProblemIdPort,
        LoadProblemForSubmissionPort {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final SpringDataProblemRepository problemRepository;

    @Override
    public List<Problem> findActiveProblemsByCategory(Long categoryId) {
        return problemRepository.findByProblemSet_Category_CategoryIdAndStatusOrderByProblemOrderAsc(
                categoryId,
                ACTIVE_STATUS
        ).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Problem> findActiveProblemByProblemSetAndOrder(Long problemSetId, Integer problemOrder) {
        return problemRepository.findByProblemSet_ProblemSetIdAndProblemOrderAndStatus(
                problemSetId,
                problemOrder,
                ACTIVE_STATUS
        ).map(this::toDomain);
    }

    @Override
    public Long loadProblemSetIdByProblemId(Long problemId) {
        ProblemJpaEntity problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        return problem.getProblemSet().getProblemSetId();
    }

    @Override
    public ProblemForSubmission loadProblemForSubmission(Long problemId) {
        ProblemJpaEntity problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        return new ProblemForSubmission(
                problem.getProblemId(),
                problem.getProblemSet().getProblemSetId(),
                problem.getProblemOrder(),
                problem.getAnswer(),
                problem.getExplanation(),
                problem.getPoint(),
                problem.getAttemptLimit(),
                problem.getRetriable()
        );
    }

    @Override
    public TestCaseProblemView loadByProblemId(Long problemId) {
        ProblemJpaEntity problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        return new TestCaseProblemView(
                problem.getProblemId(),
                problem.getProblemType()
        );
    }

    @Override
    public ExecutionProblem loadProblem(Long problemId) {
        ProblemJpaEntity problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        return new ExecutionProblem(
                problem.getProblemId(),
                problem.getProblemSet().getProblemSetId(),
                problem.getProblemType()
        );
    }

    @Override
    public Optional<Long> findNextProblemId(Long problemSetId, Integer nextProblemOrder) {
        return problemRepository
                .findByProblemSet_ProblemSetIdAndProblemOrderAndStatus(
                        problemSetId,
                        nextProblemOrder,
                        ACTIVE_STATUS
                )
                .map(ProblemJpaEntity::getProblemId);
    }

    @Override
    public List<Problem> findActiveProblemsByProblemSet(Long problemSetId) {
        return problemRepository.findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(
                problemSetId,
                ACTIVE_STATUS
        ).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Problem> findLastActiveProblem(Long problemSetId) {
        return problemRepository.findTopByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderDesc(
                problemSetId,
                ACTIVE_STATUS
        ).map(this::toDomain);
    }

    @Override
    public Optional<Problem> findById(Long problemId) {
        return problemRepository.findById(problemId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Problem> findByProblemSetAndProblemId(Long problemSetId, Long problemId) {
        return problemRepository.findByProblemIdAndProblemSet_ProblemSetId(problemId, problemSetId)
                .map(this::toDomain);
    }

    private Problem toDomain(ProblemJpaEntity entity) {
        return Problem.restore(
                entity.getProblemId(),
                entity.getProblemSet().getProblemSetId(),
                entity.getProblemOrder(),
                entity.getTitle(),
                entity.getContent(),
                entity.getProblemType(),
                entity.getAnswer(),
                entity.getExplanation(),
                entity.getPoint(),
                entity.getAttemptLimit(),
                entity.getRetriable()
        );
    }
}
