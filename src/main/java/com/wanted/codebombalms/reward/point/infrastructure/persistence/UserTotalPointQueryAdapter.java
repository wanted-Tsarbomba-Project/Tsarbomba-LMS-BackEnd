package com.wanted.codebombalms.reward.point.infrastructure.persistence;

import com.wanted.codebombalms.badge.application.port.LoadUserTotalPointPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserTotalPointQueryAdapter
        implements LoadUserTotalPointPort {

    private final SpringDataUserPointRepository userPointRepository;

    @Override
    public int loadTotalPoint(Long userId) {
        return userPointRepository.findByUserId(userId)
                .map(UserPointJpaEntity::getTotalPoint)
                .orElse(0);
    }
}
