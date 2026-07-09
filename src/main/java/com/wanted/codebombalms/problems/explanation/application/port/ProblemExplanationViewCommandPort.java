package com.wanted.codebombalms.problems.explanation.application.port;


public interface ProblemExplanationViewCommandPort {

    void saveViewed(Long userId, Long problemId, Long problemSetId);
}
