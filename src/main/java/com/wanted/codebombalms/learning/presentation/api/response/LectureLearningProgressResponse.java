package com.wanted.codebombalms.learning.presentation.api.response;

import com.wanted.codebombalms.learning.domain.model.LectureLearningProgress;

public record LectureLearningProgressResponse(
        Long lectureId,
        String lectureTitle,
        long completedStudentCount,
        long totalStudentCount,
        int progressRate
) {

    public static LectureLearningProgressResponse from(LectureLearningProgress progress) {
        return new LectureLearningProgressResponse(
                progress.lectureId(),
                progress.lectureTitle(),
                progress.completedStudentCount(),
                progress.totalStudentCount(),
                progress.progressRate()
        );
    }
}
