package com.wanted.codebombalms.reward.point.domain.model;

import java.time.LocalDateTime;

public record UserPoint(
        Long userPointId,
        Long userId,
        Integer totalPoint,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserPoint create(Long userId, Integer point) {
        return new UserPoint(null, userId, point, null, null);
    }

    public UserPoint addPoint(Integer point) {
        return new UserPoint(
                userPointId,
                userId,
                totalPoint + point,
                createdAt,
                updatedAt
        );
    }
}
