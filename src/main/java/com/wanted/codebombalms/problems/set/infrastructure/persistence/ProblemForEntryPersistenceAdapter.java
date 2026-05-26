package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemForEntryPort;
import com.wanted.codebombalms.problems.set.domain.model.ProblemDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProblemForEntryPersistenceAdapter implements LoadProblemForEntryPort {

    private final SpringDataProblemRepository problemRepository;

    @Override
    public ProblemDetail loadCurrentProblem(Long problemSetId, Integer problemNumber) {
        return problemRepository
                .findByProblemSet_ProblemSetIdAndProblemOrderAndStatus(
                        problemSetId,
                        problemNumber,
                        "ACTIVE"
                )
                .map(this::toDetail)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.NO_CURRENT_PROBLEM));
    }

    @Override
    public Optional<ProblemDetail> loadLastProblem(Long problemSetId) {
        return problemRepository
                .findTopByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderDesc(problemSetId, "ACTIVE")
                .map(this::toDetail);
    }

    private ProblemDetail toDetail(ProblemJpaEntity problem) {
        return ProblemDetail.of(
                problem.getProblemId(),
                problem.getProblemOrder(),
                problem.getTitle(),
                problem.getContent(),
                problem.getProblemType(),
                problem.getPoint()
        );
    }
}