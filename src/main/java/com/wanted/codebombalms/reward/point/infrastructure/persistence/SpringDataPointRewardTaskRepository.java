package com.wanted.codebombalms.reward.point.infrastructure.persistence;

import com.wanted.codebombalms.reward.point.domain.model.PointRewardTaskStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataPointRewardTaskRepository
        extends JpaRepository<PointRewardTaskJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select task
            from PointRewardTaskJpaEntity task
            where task.submissionId = :submissionId
            """)
    Optional<PointRewardTaskJpaEntity> findBySubmissionIdForUpdate(
            @Param("submissionId") Long submissionId
    );

    List<PointRewardTaskJpaEntity>
    findByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAscCreatedAtAsc(
            PointRewardTaskStatus status,
            LocalDateTime now,
            Pageable pageable
    );
}
