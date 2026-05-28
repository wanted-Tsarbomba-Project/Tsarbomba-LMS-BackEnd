package com.wanted.codebombalms.reward.point.infrastructure.persistence;

import com.wanted.codebombalms.reward.point.domain.model.PointHistory;
import com.wanted.codebombalms.reward.point.domain.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PointHistoryPersistenceAdapter implements PointHistoryRepository {

    private final SpringDataPointHistoryRepository springDataPointHistoryRepository;

    @Override
    public PointHistory save(PointHistory pointHistory) {
        PointHistoryJpaEntity entity = PointHistoryJpaEntity.from(pointHistory);
        return springDataPointHistoryRepository.save(entity).toDomain();
    }

    @Override
    public boolean existsByUserIdAndProblemId(Long userId, Long problemId) {
        return springDataPointHistoryRepository.existsByUserIdAndProblemId(userId, problemId);
    }

    @Override
    public List<PointHistory> findAllByUserId(Long userId) {
        return springDataPointHistoryRepository.findAllByUserId(userId).stream()
                .map(PointHistoryJpaEntity::toDomain)
                .collect(Collectors.toList());
    }
}
