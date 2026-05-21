package com.wanted.codebombalms.submission.infrastructure.progress;

import com.wanted.codebombalms.problems.progress.application.service.ProgressCommandService;
import com.wanted.codebombalms.submission.application.port.ProblemProgressPort;
import org.springframework.stereotype.Component;

@Component
public class ProblemProgressAdapter implements ProblemProgressPort {

    private final ProgressCommandService progressCommandService;

    public ProblemProgressAdapter(ProgressCommandService progressCommandService) {
        this.progressCommandService = progressCommandService;
    }

    @Override
    public void validateCurrentProblem(Long userId, Long problemSetId, Integer problemOrder) {
        progressCommandService.validateCurrentProblem(userId, problemSetId, problemOrder);
    }

    @Override
    public Integer openNextProblem(Long userId, Long problemSetId) {
        return progressCommandService.openNextProblem(userId, problemSetId);
    }

    @Override
    public void completeProblemSet(Long userId, Long problemSetId) {
        progressCommandService.completeProblemSet(userId, problemSetId);
    }
}
