package com.wanted.codebombalms.problems.explanation.application.port;

import java.util.List;
import java.util.Set;

public interface ProblemExplanationViewQueryPort {

    boolean existsViewed(Long userId, Long problemId);

    Set<Long> findViewedProblemIds(Long userId, List<Long> problemIds);
}
