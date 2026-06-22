package com.wanted.codebombalms.lecture.presentation.api.response;

import com.wanted.codebombalms.lecture.application.usecase.FinalProblemSetRecommendationUseCase.FinalProblemSetCandidateView;
import java.time.LocalDateTime;

public record FinalProblemSetCandidateResponse(
        Long problemSetId,
        Integer problemNumber,
        String title,
        String description,
        String difficulty,
        Double accuracyRate,
        LocalDateTime createdAt,
        String entryPath
) {

    public static FinalProblemSetCandidateResponse from(FinalProblemSetCandidateView candidate) {
        return new FinalProblemSetCandidateResponse(
                candidate.problemSetId(),
                candidate.problemNumber(),
                candidate.title(),
                candidate.description(),
                candidate.difficulty(),
                candidate.accuracyRate(),
                candidate.createdAt(),
                candidate.entryPath()
        );
    }
}
