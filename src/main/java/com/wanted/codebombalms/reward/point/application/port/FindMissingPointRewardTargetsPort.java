package com.wanted.codebombalms.reward.point.application.port;

import java.util.List;

public interface FindMissingPointRewardTargetsPort {

    List<MissingPointRewardTarget> findTargets(int limit);

    record MissingPointRewardTarget(
            Long userId,
            Long problemId,
            Long submissionId,
            Integer point
    ) {
    }
}
