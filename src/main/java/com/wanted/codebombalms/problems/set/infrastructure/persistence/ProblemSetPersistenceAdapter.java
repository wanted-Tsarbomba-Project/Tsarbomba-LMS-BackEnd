package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class ProblemSetPersistenceAdapter implements LoadProblemSetPort {

    private final SpringDataProblemSetRepository problemSetRepository;

    @Override
    public List<ProblemSetSummary> loadActiveProblemSetsByCategory(Long categoryId) {
        var problemSets = problemSetRepository
                .findByCategory_CategoryIdAndStatusOrderByProblemSetIdAsc(categoryId, "ACTIVE");

        return IntStream.range(0, problemSets.size())
                .mapToObj(index -> ProblemSetMapper.toSummary(problemSets.get(index), index + 1))
                .toList();
    }

}
