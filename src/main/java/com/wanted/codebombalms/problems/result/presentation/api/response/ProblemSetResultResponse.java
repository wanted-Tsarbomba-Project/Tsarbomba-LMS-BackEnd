package com.wanted.codebombalms.problems.result.presentation.api.response;

import com.wanted.codebombalms.problems.result.domain.model.ProblemSetResult;

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
    public ProblemSetResultResponse(ProblemSetResult result) {
        this(
                result.getProblemSetId(),
                result.getTitle(),
                result.getCompleted(),
                result.getAccuracyRate(),
                result.getTotalCompletedUserCount(),
                result.getCorrectCompletedUserCount(),
                result.getSubmissions()
                        .stream()
                        .map(ProblemSubmissionResultResponse::new)
                        .toList()
        );
    }
}
