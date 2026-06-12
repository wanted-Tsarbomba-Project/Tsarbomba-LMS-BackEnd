package com.wanted.codebombalms.reward.point.application.usecase;

public interface SchedulePointRewardTaskUseCase {

    void schedule(
            Long userId,
            Long problemId,
            Long submissionId,
            Integer point
    );
}
