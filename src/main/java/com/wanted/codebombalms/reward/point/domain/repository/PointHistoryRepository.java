package com.wanted.codebombalms.reward.point.domain.repository;

import com.wanted.codebombalms.reward.point.domain.model.PointHistory;
import java.util.List;
import java.util.Optional;

public interface PointHistoryRepository {
    PointHistory save(PointHistory pointHistory);
    boolean existsByUserIdAndProblemId(Long userId, Long problemId);
    List<PointHistory> findAllByUserId(Long userId);
}
