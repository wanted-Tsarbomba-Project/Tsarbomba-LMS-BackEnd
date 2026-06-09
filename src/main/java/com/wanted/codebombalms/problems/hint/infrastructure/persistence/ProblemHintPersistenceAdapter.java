package com.wanted.codebombalms.problems.hint.infrastructure.persistence;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.hint.application.port.LoadProblemHintPort;
import com.wanted.codebombalms.problems.hint.domain.model.ProblemHint;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.set.application.port.LoadHintForUpdatePort;
import com.wanted.codebombalms.problems.set.application.port.ManageProblemSetHintsPort;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProblemHintPersistenceAdapter implements
        LoadProblemHintPort,
        LoadHintForUpdatePort,
        ManageProblemSetHintsPort {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final SpringDataProblemHintRepository repository;
    private final EntityManager entityManager;

    @Override
    public List<ProblemHint> findByProblemId(Long problemId) {
        return repository.findByProblem_ProblemIdAndProblem_StatusOrderByHintOrderAsc(problemId, ACTIVE_STATUS)
                .stream()
                .map(entity -> ProblemHint.restore(
                        entity.getHintId(),
                        entity.getHintOrder(),
                        entity.getHintContent()
                ))
                .toList();
    }

    @Override
    public Optional<HintForUpdateData> loadFirstHintForUpdate(Long problemId) {
        return repository.findByProblem_ProblemIdAndProblem_StatusOrderByHintOrderAsc(problemId, ACTIVE_STATUS)
                .stream()
                .findFirst()
                .map(hint -> new HintForUpdateData(
                        hint.getHintId(),
                        hint.getHintContent()
                ));
    }

    @Override
    public void createHint(Long problemId, String hintContent) {
        if (hintContent == null || hintContent.isBlank()) {
            return;
        }

        ProblemJpaEntity problem = entityManager.getReference(ProblemJpaEntity.class, problemId);
        repository.save(new ProblemHintJpaEntity(problem, 1, hintContent));
    }

    @Override
    public void updateOrCreateHint(Long problemId, Long hintId, String hintContent) {
        if (hintContent == null || hintContent.isBlank()) {
            return;
        }

        if (hintId == null) {
            createHint(problemId, hintContent);
            return;
        }

        ProblemHintJpaEntity hint = repository
                .findByHintIdAndProblem_ProblemId(hintId, problemId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        hint.update(hintContent);
    }
}
