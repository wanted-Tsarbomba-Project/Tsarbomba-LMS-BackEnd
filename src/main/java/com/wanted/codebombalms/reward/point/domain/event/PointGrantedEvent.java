package com.wanted.codebombalms.reward.point.domain.event;

public record PointGrantedEvent(
        Long userId,
        Integer totalPoint
) {
}
