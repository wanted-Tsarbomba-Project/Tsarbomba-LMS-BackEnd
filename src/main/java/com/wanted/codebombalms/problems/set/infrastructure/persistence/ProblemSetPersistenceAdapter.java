package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetBrief;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.progress.application.port.CheckProgressProblemSetPort;
import com.wanted.codebombalms.problems.set.application.port.IncreaseProblemSetCompletedCountPort;
import com.wanted.codebombalms.problems.set.application.port.IncreaseProblemSetStartedCountPort;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetStatus;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummaryPage;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class ProblemSetPersistenceAdapter implements
        LoadProblemSetPort,
        IncreaseProblemSetCompletedCountPort,
        CheckProgressProblemSetPort,
        IncreaseProblemSetStartedCountPort {

    private final SpringDataProblemSetRepository problemSetRepository;

    @Override
    public ProblemSetSummaryPage loadActiveProblemSetsByCategory(Long categoryId, int page, int size) {
        var problemSets = problemSetRepository.findByCategory_CategoryIdAndStatusOrderByProblemSetIdAsc(
                categoryId,
                ProblemSetStatus.ACTIVE,
                PageRequest.of(page, size)
        );

        return toSummaryPage(problemSets, page, size);
    }

    @Override
    public void checkProblemSetExists(Long problemSetId) {
        if (!problemSetRepository.existsById(problemSetId)) {
            throw new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND);
        }
    }

    @Override
    public void increaseCompletedUserCount(Long problemSetId) {
        ProblemSetJpaEntity problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        problemSet.increaseCompletedUserCount();

        problemSetRepository.save(problemSet);
    }

    @Override
    public void increaseStartedUserCount(Long problemSetId) {
        ProblemSetJpaEntity problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        problemSet.increaseStartedUserCount();

        problemSetRepository.save(problemSet);
    }

    @Override
    public ProblemSetSummaryPage loadActiveProblemSets(int page, int size) {
        var problemSets = problemSetRepository.findByStatusOrderByProblemSetIdAsc(
                ProblemSetStatus.ACTIVE,
                PageRequest.of(page, size)
        );

        return toSummaryPage(problemSets, page, size);
    }

    private ProblemSetSummaryPage toSummaryPage(
            Page<ProblemSetJpaEntity> problemSets,
            int page,
            int size
    ) {
        int startNumber = page * size + 1;

        var content = IntStream.range(0, problemSets.getContent().size())
                .mapToObj(index -> ProblemSetMapper.toSummary(
                        problemSets.getContent().get(index),
                        startNumber + index
                ))
                .toList();

        return new ProblemSetSummaryPage(
                content,
                page,
                size,
                problemSets.getTotalElements(),
                problemSets.getTotalPages(),
                problemSets.hasNext()
        );
    }

    @Override
    public Optional<ProblemSetBrief> loadById(Long problemSetId) {
        return problemSetRepository.findById(problemSetId)
                .map(ProblemSetMapper::toBrief);
    }

}
