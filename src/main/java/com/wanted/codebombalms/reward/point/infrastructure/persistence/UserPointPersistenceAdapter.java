package com.wanted.codebombalms.reward.point.infrastructure.persistence;

import com.wanted.codebombalms.reward.point.domain.model.UserPoint;
import com.wanted.codebombalms.reward.point.domain.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserPointPersistenceAdapter implements UserPointRepository {

    private final SpringDataUserPointRepository springDataUserPointRepository;

    @Override
    public UserPoint save(UserPoint userPoint) {
        return springDataUserPointRepository.save(UserPointJpaEntity.from(userPoint))
                .toDomain();
    }

    @Override
    public Optional<UserPoint> findByUserIdForUpdate(Long userId) {
        return springDataUserPointRepository.findByUserIdForUpdate(userId)
                .map(UserPointJpaEntity::toDomain);
    }
}
