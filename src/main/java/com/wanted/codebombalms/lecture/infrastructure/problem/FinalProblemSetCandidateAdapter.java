package com.wanted.codebombalms.lecture.infrastructure.problem;

import com.wanted.codebombalms.lecture.application.port.FinalProblemSetCandidatePort;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetStatus;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetMapper;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetJpaEntity;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.SpringDataProblemSetRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinalProblemSetCandidateAdapter implements FinalProblemSetCandidatePort {

    private final SpringDataProblemSetRepository problemSetRepository;

    @Override
    public List<ProblemSetSummary> findCandidates(Long problemCategoryId, Set<Long> excludedProblemSetIds, int limit) {
        AtomicInteger problemNumber = new AtomicInteger(1);

        return problemSetRepository.findByCategory_CategoryIdAndStatusOrderByProblemSetIdAsc(
                        problemCategoryId,
                        ProblemSetStatus.ACTIVE
                )
                .stream()
                .filter(problemSet -> !excludedProblemSetIds.contains(problemSet.getProblemSetId()))
                .limit(limit)
                .map(problemSet -> ProblemSetMapper.toSummary(problemSet, problemNumber.getAndIncrement()))
                .toList();
    }

    @Override
    public List<ProblemSetSummary> findByProblemSetIds(List<Long> problemSetIds) {
        if (problemSetIds.isEmpty()) {
            return List.of();
        }

        Map<Long, ProblemSetJpaEntity> entitiesById = problemSetRepository.findByProblemSetIdInAndStatus(
                        problemSetIds,
                        ProblemSetStatus.ACTIVE
                )
                .stream()
                .collect(Collectors.toMap(
                        ProblemSetJpaEntity::getProblemSetId,
                        Function.identity()
                ));

        AtomicInteger problemNumber = new AtomicInteger(1);
        return problemSetIds.stream()
                .map(entitiesById::get)
                .filter(java.util.Objects::nonNull)
                .map(entity -> ProblemSetMapper.toSummary(entity, problemNumber.getAndIncrement()))
                .toList();
    }
}
