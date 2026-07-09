package com.wanted.codebombalms.problems.explanation.application.usecase;

import com.wanted.codebombalms.problems.progress.enums.ProblemProgressStatus;

public interface ViewProblemExplanationUseCase {

    ExplanationView viewExplanation(Long userId, Long problemId);

    record ExplanationView(
            Long problemId,
            ProblemProgressStatus status,
            ProblemProgressStatus displayStatus,
            String explanation,
            Long nextProblemId,
            boolean problemSetCompleted,
            int earnedPoint,
            boolean pointGranted
    ) {
    }
}
