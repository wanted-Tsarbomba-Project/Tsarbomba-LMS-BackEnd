package com.wanted.codebombalms.learning.presentation.api.response;

import com.wanted.codebombalms.learning.domain.model.StudentLearningProgress;

public record StudentLearningProgressResponse(
        Long userId,
        String studentName,
        long completedLectureCount,
        long totalLectureCount,
        int lectureProgressRate,
        long completedProblemCount,
        long totalProblemCount
) {

    public static StudentLearningProgressResponse from(StudentLearningProgress progress) {
        return new StudentLearningProgressResponse(
                progress.userId(),
                progress.studentName(),
                progress.completedLectureCount(),
                progress.totalLectureCount(),
                progress.lectureProgressRate(),
                progress.completedProblemCount(),
                progress.totalProblemCount()
        );
    }
}
