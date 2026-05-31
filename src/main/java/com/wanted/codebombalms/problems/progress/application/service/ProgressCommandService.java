package com.wanted.codebombalms.problems.progress.application.service;

import com.wanted.codebombalms.problems.progress.application.port.CheckProgressProblemSetPort;
import com.wanted.codebombalms.problems.progress.application.port.ProgressManagementPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProgressCommandService {

    private final ProgressManagementPort progressManagementPort;
    private final CheckProgressProblemSetPort checkProgressProblemSetPort;

    @Transactional
    public void validateCurrentProblem(Long userId, Long problemSetId, Integer problemOrder) {
        checkProgressProblemSetPort.checkProblemSetExists(problemSetId);
        progressManagementPort.validateCurrentProblem(userId, problemSetId, problemOrder);
    }

    @Transactional
    public Integer openNextProblem(Long userId, Long problemSetId) {
        checkProgressProblemSetPort.checkProblemSetExists(problemSetId);
        return progressManagementPort.openNextProblem(userId, problemSetId);
    }

    @Transactional
    public void completeProblemSet(Long userId, Long problemSetId) {
        checkProgressProblemSetPort.checkProblemSetExists(problemSetId);
        progressManagementPort.completeProblemSet(userId, problemSetId);
    }
}
