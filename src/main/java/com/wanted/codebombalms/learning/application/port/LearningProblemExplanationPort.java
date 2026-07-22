package com.wanted.codebombalms.learning.application.port;

import java.util.List;
import java.util.Set;

public interface LearningProblemExplanationPort {

    boolean existsViewed(Long userId, Long problemId);

    Set<Long> findViewedProblemIds(Long userId, List<Long> problemIds);

    void saveViewed(Long userId, Long problemId, Long problemSetId);
}
