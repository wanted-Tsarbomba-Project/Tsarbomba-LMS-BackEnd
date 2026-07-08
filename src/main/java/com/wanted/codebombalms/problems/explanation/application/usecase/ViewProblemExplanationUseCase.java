package com.wanted.codebombalms.problems.explanation.application.usecase;

public interface ViewProblemExplanationUseCase {

    ExplanationView viewExplanation(Long userId, Long problemId);

    record ExplanationView(
            Long problemId,
            String status,
            String displayStatus,
            String explanation,
            Long nextProblemId,
            boolean problemSetCompleted,
            int earnedPoint,
            boolean pointGranted
    ) {
    }
}
