package com.wanted.codebombalms.learning.domain.model;

public record CourseLearningProgress(
        Long courseId,
        String courseTitle,
        long enrolledStudentCount,
        long completedLectureCount,
        long totalLectureCount,
        int averageLectureProgressRate,
        long completedProblemCount,
        long totalProblemCount
) {

    public static CourseLearningProgress of(
            Long courseId,
            String courseTitle,
            long enrolledStudentCount,
            long completedLectureCount,
            long totalLectureCount,
            long completedProblemCount,
            long totalProblemCount
    ) {
        int averageLectureProgressRate = totalLectureCount == 0
                ? 0
                : (int) ((completedLectureCount * 100) / totalLectureCount);

        return new CourseLearningProgress(
                courseId,
                courseTitle,
                enrolledStudentCount,
                completedLectureCount,
                totalLectureCount,
                averageLectureProgressRate,
                completedProblemCount,
                totalProblemCount
        );
    }
}
