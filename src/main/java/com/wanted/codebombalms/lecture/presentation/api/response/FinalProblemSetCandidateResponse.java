package com.wanted.codebombalms.lecture.presentation.api.response;

import com.wanted.codebombalms.lecture.application.usecase.FinalProblemSetRecommendationUseCase.FinalProblemSetCandidateView;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public record FinalProblemSetCandidateResponse(
        Long problemSetId,
        Integer problemNumber,
        String title,
        String description,
        String difficulty,
        Double accuracyRate,
        LocalDateTime createdAt,
        String entryPath,
        BigDecimal score,
        String reasonCode,
        String recommendationReason
) {

    private static final String PROBLEM_SET_ENTRY_PATH_PREFIX = "/api/v1/problem-sets/";

    public static FinalProblemSetCandidateResponse from(FinalProblemSetCandidateView candidate) {
        return new FinalProblemSetCandidateResponse(
                candidate.problemSetId(),
                candidate.problemNumber(),
                candidate.title(),
                candidate.description(),
                candidate.difficulty(),
                candidate.accuracyRate(),
                candidate.createdAt(),
                PROBLEM_SET_ENTRY_PATH_PREFIX + candidate.problemSetId(),
                candidate.score(),
                candidate.reasonCode(),
                candidate.recommendationReason()
        );
    }
}
