package com.wanted.codebombalms.problems.result.presentation.api.response;

import com.wanted.codebombalms.problems.result.application.usecase.GetProblemSetResultUseCase.ProblemSetResultView;

import java.util.List;

public record ProblemSetResultResponse(
        Long problemSetId,
        String title,
        Boolean isCompleted,
        Double accuracyRate,
        Integer totalCompletedUserCount,
        Integer correctCompletedUserCount,
        List<ProblemSubmissionResultResponse> submissions
) {
    public ProblemSetResultResponse(ProblemSetResultView result) {
        this(
                result.problemSetId(),
                result.title(),
                result.isCompleted(),
                result.accuracyRate(),
                result.totalCompletedUserCount(),
                result.correctCompletedUserCount(),
                result.submissions()
                        .stream()
                        .map(ProblemSubmissionResultResponse::new)
                        .toList()
        );
    }
}
