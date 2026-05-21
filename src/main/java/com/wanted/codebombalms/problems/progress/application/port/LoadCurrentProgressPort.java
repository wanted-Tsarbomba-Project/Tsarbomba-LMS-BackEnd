package com.wanted.codebombalms.problems.progress.application.port;

public interface LoadCurrentProgressPort {

    Integer loadCurrentProblemNumber(Long userId, Long problemSetId);
}
