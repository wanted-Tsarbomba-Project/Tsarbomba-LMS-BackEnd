package com.wanted.codebombalms.problems.set.application.port;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LoadHintForUpdatePort {

    Map<Long, HintForUpdateData> loadFirstHintsForUpdate(List<Long> problemIds);

    record HintForUpdateData(
            Long problemId,
            Long hintId,
            String hintContent
    ) {
    }
}
