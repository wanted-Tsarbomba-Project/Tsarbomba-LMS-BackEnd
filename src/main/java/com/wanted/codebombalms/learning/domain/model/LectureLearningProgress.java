package com.wanted.codebombalms.learning.domain.model;

public record LectureLearningProgress(
        Long lectureId,
        String lectureTitle,
        long completedStudentCount,
        long totalStudentCount,
        int progressRate
) {

    public static LectureLearningProgress of(
            Long lectureId,
            String lectureTitle,
            long completedStudentCount,
            long totalStudentCount
    ) {
        int progressRate = totalStudentCount == 0
                ? 0
                : (int) ((completedStudentCount * 100) / totalStudentCount);

        return new LectureLearningProgress(
                lectureId,
                lectureTitle,
                completedStudentCount,
                totalStudentCount,
                progressRate
        );
    }
}
