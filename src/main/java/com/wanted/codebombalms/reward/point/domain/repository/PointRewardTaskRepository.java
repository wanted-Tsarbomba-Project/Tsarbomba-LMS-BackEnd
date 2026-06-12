package com.wanted.codebombalms.reward.point.domain.repository;

import com.wanted.codebombalms.reward.point.domain.model.PointRewardTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PointRewardTaskRepository {

    PointRewardTask save(PointRewardTask task);

    Optional<PointRewardTask> findBySubmissionIdForUpdate(Long submissionId);

    List<PointRewardTask> findRecoverableTasks(
            LocalDateTime now,
            int limit
    );
}
