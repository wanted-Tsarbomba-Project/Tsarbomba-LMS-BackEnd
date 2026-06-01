package com.wanted.codebombalms.reward.point.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataPointHistoryRepository extends JpaRepository<PointHistoryJpaEntity, Long> {
    boolean existsByUserIdAndProblemId(Long userId, Long problemId);
    List<PointHistoryJpaEntity> findAllByUserId(Long userId);
}
