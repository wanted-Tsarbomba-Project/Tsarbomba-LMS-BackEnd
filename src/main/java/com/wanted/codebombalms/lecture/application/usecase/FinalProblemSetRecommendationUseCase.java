package com.wanted.codebombalms.lecture.application.usecase;

import java.time.LocalDateTime;
import java.util.List;

public interface FinalProblemSetRecommendationUseCase {

    List<FinalProblemSetCandidateView> findFinalProblemSetCandidates(Long lectureId, Long userId, boolean operator);

    record FinalProblemSetCandidateView(
            Long problemSetId,
            Integer problemNumber,
            String title,
            String description,
            String difficulty,
            Double accuracyRate,
            LocalDateTime createdAt
    ) {
    }
}
