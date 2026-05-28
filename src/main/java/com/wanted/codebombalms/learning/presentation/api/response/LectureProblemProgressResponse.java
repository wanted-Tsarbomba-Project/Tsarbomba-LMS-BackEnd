package com.wanted.codebombalms.learning.presentation.api.response;

import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import java.time.LocalDateTime;

public record LectureProblemProgressResponse(
        Long lectureProblemProgressId,
        Long lectureProblemSetId,
        Integer currentProblemNumber,
        boolean completed,
        LocalDateTime completedAt
) {

    public static LectureProblemProgressResponse from(LectureProblemProgress progress) {
        return new LectureProblemProgressResponse(
                progress.getLectureProblemProgressId(),
                progress.getLectureProblemSetId(),
                progress.getCurrentProblemNumber(),
                progress.isCompleted(),
                progress.getCompletedAt()
        );
    }
}
