package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LoadProblemSetAccessPort {

    Optional<ProblemSetAccessData> loadAccessData(Long problemSetId);

    record ProblemSetAccessData(
            ProblemSetStatus status,
            LocalDateTime deletedAt
    ) {
        public boolean isActive() {
            return status == ProblemSetStatus.ACTIVE
                    && deletedAt == null;
        }
    }
}
