package com.wanted.codebombalms.problems.problem.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.problem.application.port.ProblemTargetDetailPort;
import com.wanted.codebombalms.problems.problem.application.port.ProblemTargetDetailPort.ProblemTargetDetailView;
import com.wanted.codebombalms.problems.problem.application.usecase.ProblemTargetDetailQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemTargetDetailQueryService implements ProblemTargetDetailQueryUseCase {

    private final ProblemTargetDetailPort problemTargetDetailPort;

    @Override
    public ProblemTargetDetailView findProblemTargetDetail(Long problemId) {
        return problemTargetDetailPort.findProblemTargetDetail(problemId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));
    }
}
