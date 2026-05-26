package com.wanted.codebombalms.problems.problem.infrastructure.persistence;

import com.wanted.codebombalms.problems.problem.domain.model.Problem;
import com.wanted.codebombalms.problems.problem.domain.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProblemPersistenceAdapter implements ProblemRepository {

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