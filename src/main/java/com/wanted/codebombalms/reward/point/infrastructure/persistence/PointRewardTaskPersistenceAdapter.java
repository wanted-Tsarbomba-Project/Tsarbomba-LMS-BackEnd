package com.wanted.codebombalms.reward.point.infrastructure.persistence;

import com.wanted.codebombalms.reward.point.domain.model.PointRewardTask;
import com.wanted.codebombalms.reward.point.domain.model.PointRewardTaskStatus;
import com.wanted.codebombalms.reward.point.domain.repository.PointRewardTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointRewardTaskPersistenceAdapter
        implements PointRewardTaskRepository {

    private final SpringDataPointRewardTaskRepository repository;

    @Override
    public PointRewardTask save(PointRewardTask task) {
        return repository.save(PointRewardTaskJpaEntity.from(task)).toDomain();
    }

    @Override
    public Optional<PointRewardTask> findBySubmissionIdForUpdate(Long submissionId) {
        return repository.findBySubmissionIdForUpdate(submissionId)
                .map(PointRewardTaskJpaEntity::toDomain);
    }

    @Override
    public List<PointRewardTask> findRecoverableTasks(
            LocalDateTime now,
            int limit
    ) {
        return repository
                .findByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAscCreatedAtAsc(
                        PointRewardTaskStatus.PENDING,
                        now,
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(PointRewardTaskJpaEntity::toDomain)
                .toList();
    }
}
