package com.wanted.codebombalms.problems.set.application.port;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LoadProblemSetAccessPort {

    Optional<ProblemSetAccessData> loadAccessData(Long problemSetId);

    record ProblemSetAccessData(
            String status,
            LocalDateTime deletedAt
    ) {
        public boolean isActive() {
            return "ACTIVE".equals(status) && deletedAt == null;
        }
    }
}
