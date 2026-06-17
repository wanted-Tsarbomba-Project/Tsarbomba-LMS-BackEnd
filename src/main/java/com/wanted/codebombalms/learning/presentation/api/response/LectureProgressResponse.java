package com.wanted.codebombalms.learning.presentation.api.response;

import com.wanted.codebombalms.learning.domain.model.LectureProgress;
import java.time.LocalDateTime;

public record LectureProgressResponse(
        Long lectureProgressId,
        Long lectureId,
        boolean completed,
        LocalDateTime completedAt,
        LocalDateTime lastWatchedAt,
        int lastPositionSec,
        Integer durationSec,
        int watchedSec
) {

    public static LectureProgressResponse from(LectureProgress progress) {
        return new LectureProgressResponse(
                progress.getLectureProgressId(),
                progress.getLectureId(),
                progress.isCompleted(),
                progress.getCompletedAt(),
                progress.getLastWatchedAt(),
                progress.getLastPositionSec(),
                progress.getDurationSec(),
                progress.getWatchedSec()
        );
    }
}
