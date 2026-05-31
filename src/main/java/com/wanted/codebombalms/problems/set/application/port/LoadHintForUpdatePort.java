package com.wanted.codebombalms.problems.set.application.port;

import java.util.Optional;

public interface LoadHintForUpdatePort {

    Optional<HintForUpdateData> loadFirstHintForUpdate(Long problemId);

    record HintForUpdateData(
            Long hintId,
            String hintContent
    ) {
    }
}
