package com.wanted.codebombalms.problems.progress.application.service;

import com.wanted.codebombalms.problems.progress.application.port.ProgressManagementPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressCommandService {

    private final ProgressManagementPort progressManagementPort;

    public ProgressCommandService(ProgressManagementPort progressManagementPort) {
        this.progressManagementPort = progressManagementPort;
    }

    @Transactional
    public void validateCurrentProblem(Long userId, Long problemSetId, Integer problemOrder) {
        progressManagementPort.validateCurrentProblem(userId, problemSetId, problemOrder);
    }

    @Transactional
    public Integer openNextProblem(Long userId, Long problemSetId) {
        return progressManagementPort.openNextProblem(userId, problemSetId);
    }

    @Transactional
    public void completeProblemSet(Long userId, Long problemSetId) {
        progressManagementPort.completeProblemSet(userId, problemSetId);
    }
}
