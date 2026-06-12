package com.wanted.codebombalms.reward.point.infrastructure.persistence;

import com.wanted.codebombalms.reward.point.domain.model.PointRewardTaskStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataPointRewardTaskRepository
        extends JpaRepository<PointRewardTaskJpaEntity, Long> {

    Optional<PointRewardTaskJpaEntity> findBySubmissionId(Long submissionId);

    List<PointRewardTaskJpaEntity>
    findByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
            PointRewardTaskStatus status,
            LocalDateTime now,
            Pageable pageable
    );
}
