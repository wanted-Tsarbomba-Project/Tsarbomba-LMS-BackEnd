package com.wanted.codebombalms.problems.problem.infrastructure.persistence;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.execution.application.port.LoadExecutionProblemPort;
import com.wanted.codebombalms.problems.problem.application.port.LoadProblemSetIdByProblemIdPort;
import com.wanted.codebombalms.problems.problem.domain.model.Problem;
import com.wanted.codebombalms.problems.problem.domain.repository.ProblemRepository;
import com.wanted.codebombalms.problems.progress.application.port.LoadProblemsForProgressPort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemForEntryPort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemsForUpdatePort;
import com.wanted.codebombalms.problems.set.application.port.ManageProblemSetProblemsPort;
import com.wanted.codebombalms.problems.set.domain.model.ProblemDetail;
import com.wanted.codebombalms.problems.set.domain.model.ProblemModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemRegistration;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetJpaEntity;
import com.wanted.codebombalms.problems.testcase.application.port.LoadTestCaseProblemPort;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProblemPersistenceAdapter implements
        ProblemRepository,
        LoadTestCaseProblemPort,
        LoadExecutionProblemPort,
        LoadProblemSetIdByProblemIdPort,
        LoadProblemForSubmissionPort,
        LoadProblemsForProgressPort,
        LoadProblemForEntryPort,
        LoadProblemsForUpdatePort,
        ManageProblemSetProblemsPort {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final SpringDataProblemRepository problemRepository;
    private final EntityManager entityManager;

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
    public List<ProblemForUpdateData> loadProblemsForUpdate(Long problemSetId) {
        return problemRepository
                .findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(
                        problemSetId,
                        ACTIVE_STATUS
                )
                .stream()
                .map(problem -> new ProblemForUpdateData(
                        problem.getProblemId(),
                        problem.getTitle(),
                        problem.getContent(),
                        problem.getPoint(),
                        problem.getExplanation()
                ))
                .toList();
    }

    @Override
    public List<ProgressProblem> loadActiveProblems(Long problemSetId) {
        return problemRepository
                .findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(
                        problemSetId,
                        ACTIVE_STATUS
                )
                .stream()
                .map(problem -> new ProgressProblem(
                        problem.getProblemId(),
                        problem.getProblemOrder(),
                        problem.getTitle()
                ))
                .toList();
    }

    @Override
    public ProblemForSubmission loadProblemForSubmission(Long problemId) {
        ProblemJpaEntity problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        return new ProblemForSubmission(
                problem.getProblemId(),
                problem.getProblemSet().getProblemSetId(),
                problem.getProblemOrder(),
                problem.getExplanation(),
                problem.getPoint(),
                problem.getAttemptLimit(),
                problem.getRetriable()
        );
    }

    @Override
    public List<ProblemDetail> loadProblems(Long problemSetId) {
        return problemRepository
                .findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(problemSetId, ACTIVE_STATUS)
                .stream()
                .map(this::toProblemDetail)
                .toList();
    }

    @Override
    public Long createProblem(Long problemSetId, ProblemRegistration command, Integer problemOrder) {
        ProblemSetJpaEntity problemSet = getProblemSetReference(problemSetId);
        ProblemJpaEntity problem = problemRepository.save(toProblemEntity(problemSet, command, problemOrder));

        return problem.getProblemId();
    }

    @Override
    public Long updateOrCreateProblem(Long problemSetId, ProblemModification command, Integer problemOrder) {
        if (command.problemId() == null) {
            ProblemSetJpaEntity problemSet = getProblemSetReference(problemSetId);
            ProblemJpaEntity problem = problemRepository.save(toProblemEntity(problemSet, command, problemOrder));

            return problem.getProblemId();
        }

        ProblemJpaEntity problem = problemRepository
                .findByProblemIdAndProblemSet_ProblemSetId(command.problemId(), problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        problem.update(
                command.title(),
                command.content(),
                command.problemType(),
                command.difficulty(),
                command.explanation(),
                command.point(),
                command.attemptLimit(),
                command.isRetriable()
        );

        return problem.getProblemId();
    }

    @Override
    public List<Long> deactivateProblemsNotIn(Long problemSetId, Set<Long> retainedProblemIds) {
        List<ProblemJpaEntity> problems = problemRepository.findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(
                problemSetId,
                ACTIVE_STATUS
        ).stream()
                .filter(problem -> !retainedProblemIds.contains(problem.getProblemId()))
                .toList();

        problems.forEach(ProblemJpaEntity::deactivate);

        return problems.stream()
                .map(ProblemJpaEntity::getProblemId)
                .toList();
    }

    @Override
    public List<Long> deactivateActiveProblems(Long problemSetId) {
        List<ProblemJpaEntity> problems = problemRepository.findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(
                problemSetId,
                ACTIVE_STATUS
        );

        problems.forEach(ProblemJpaEntity::deactivate);

        return problems.stream()
                .map(ProblemJpaEntity::getProblemId)
                .toList();
    }

    private ProblemSetJpaEntity getProblemSetReference(Long problemSetId) {
        return entityManager.getReference(ProblemSetJpaEntity.class, problemSetId);
    }

    private ProblemJpaEntity toProblemEntity(
            ProblemSetJpaEntity problemSet,
            ProblemRegistration command,
            Integer problemOrder
    ) {
        return new ProblemJpaEntity(
                problemSet,
                command.title(),
                command.content(),
                command.problemType(),
                command.difficulty(),
                command.explanation(),
                command.point(),
                command.attemptLimit(),
                command.isRetriable(),
                problemOrder
        );
    }

    private ProblemJpaEntity toProblemEntity(
            ProblemSetJpaEntity problemSet,
            ProblemModification command,
            Integer problemOrder
    ) {
        return new ProblemJpaEntity(
                problemSet,
                command.title(),
                command.content(),
                command.problemType(),
                command.difficulty(),
                command.explanation(),
                command.point(),
                command.attemptLimit(),
                command.isRetriable(),
                problemOrder
        );
    }

    private ProblemDetail toProblemDetail(ProblemJpaEntity problem) {
        return ProblemDetail.of(
                problem.getProblemId(),
                problem.getProblemOrder(),
                problem.getTitle(),
                problem.getContent(),
                problem.getProblemType(),
                problem.getPoint()
        );
    }

    @Override
    public TestCaseProblemView loadByProblemId(Long problemId) {
        ProblemJpaEntity problem = problemRepository.findByProblemIdAndStatus(problemId, ACTIVE_STATUS)
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
                entity.getExplanation(),
                entity.getPoint(),
                entity.getAttemptLimit(),
                entity.getRetriable()
        );
    }
}
