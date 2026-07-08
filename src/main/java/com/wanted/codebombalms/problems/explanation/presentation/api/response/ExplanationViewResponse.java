package com.wanted.codebombalms.problems.explanation.presentation.api.response;

import com.wanted.codebombalms.problems.explanation.application.usecase.ViewProblemExplanationUseCase;

public record ExplanationViewResponse(
        Long problemId,
        String status,
        String displayStatus,
        String explanation,
        Long nextProblemId,
        boolean problemSetCompleted,
        int earnedPoint,
        boolean pointGranted
) {
    public static ExplanationViewResponse from(ViewProblemExplanationUseCase.ExplanationView view) {
        return new ExplanationViewResponse(
                view.problemId(),
                view.status(),
                view.displayStatus(),
                view.explanation(),
                view.nextProblemId(),
                view.problemSetCompleted(),
                view.earnedPoint(),
                view.pointGranted()
        );
    }
}
