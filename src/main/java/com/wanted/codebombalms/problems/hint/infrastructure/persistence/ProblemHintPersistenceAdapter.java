package com.wanted.codebombalms.problems.hint.infrastructure.persistence;

import com.wanted.codebombalms.problems.hint.domain.model.ProblemHint;
import com.wanted.codebombalms.problems.hint.application.port.LoadProblemHintPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
@RequiredArgsConstructor
public class ProblemHintPersistenceAdapter implements LoadProblemHintPort {

    private final SpringDataProblemHintRepository repository;

    @Override
    public List<ProblemHint> findByProblemId(Long problemId) {
        return repository.findByProblem_ProblemIdOrderByHintOrderAsc(problemId)
                .stream()
                .map(entity -> ProblemHint.restore(
                        entity.getHintId(),
                        entity.getHintOrder(),
                        entity.getHintContent()
                ))
                .toList();
    }
}
