package com.wanted.codebombalms.reward.point.application.usecase;

public interface GrantProblemPointUseCase {

    void grant(Long userId, Long problemId, Long submissionId, Integer point);
}
