package com.wanted.codebombalms.problems.explanation.infrastructure.persistence;

import com.wanted.codebombalms.problems.explanation.application.port.ProblemExplanationViewCommandPort;
import com.wanted.codebombalms.problems.explanation.application.port.ProblemExplanationViewQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProblemExplanationViewPersistenceAdapter
        implements ProblemExplanationViewQueryPort, ProblemExplanationViewCommandPort {

    private final SpringDataProblemExplanationViewRepository repository;

    @Override
    public boolean existsViewed(Long userId, Long problemId) {
        return repository.existsByUserIdAndProblemId(userId, problemId);
    }

    @Override
    public Set<Long> findViewedProblemIds(Long userId, List<Long> problemIds) {
        return repository.findByUserIdAndProblemIdIn(userId, problemIds)
                .stream()
                .map(ProblemExplanationViewJpaEntity::getProblemId)
                .collect(Collectors.toSet());
    }

    @Override
    public void saveViewed(Long userId, Long problemId, Long problemSetId) {
        repository.saveViewedIgnoreDuplicate(userId, problemId, problemSetId);
    }
}
