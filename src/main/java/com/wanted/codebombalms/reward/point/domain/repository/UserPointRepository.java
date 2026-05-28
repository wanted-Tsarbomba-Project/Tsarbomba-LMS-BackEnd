package com.wanted.codebombalms.reward.point.domain.repository;

import com.wanted.codebombalms.reward.point.domain.model.UserPoint;

import java.util.Optional;

public interface UserPointRepository {

    UserPoint save(UserPoint userPoint);

    Optional<UserPoint> findByUserIdForUpdate(Long userId);
}
