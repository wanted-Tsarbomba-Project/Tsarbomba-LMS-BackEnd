package com.wanted.codebombalms.problems.set.application.port;

import java.util.List;

public interface LoadProblemsForUpdatePort {

    List<ProblemForUpdateData> loadProblemsForUpdate(Long problemSetId);

    record ProblemForUpdateData(
            Long problemId,
            String title,
            String content,
            Integer point,
            String answer,
            String explanation
    ) {
    }
}
