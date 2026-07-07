package com.wanted.codebombalms.enrollment.application.port;

import java.util.Collection;
import java.util.Map;

public interface EnrollmentLearningProgressPort {

    Map<EnrollmentLearningProgressKey, EnrollmentLearningProgress> findProgresses(
            Collection<EnrollmentLearningProgressKey> keys
    );

    record EnrollmentLearningProgressKey(
            Long userId,
            Long courseId
    ) {
    }

    record EnrollmentLearningProgress(
            boolean learningCompleted,
            String displayStatus,
            int lectureProgressRate,
            long completedLectureCount,
            long totalLectureCount,
            long completedProblemCount,
            long totalProblemCount
    ) {

        public static EnrollmentLearningProgress of(
                long completedLectureCount,
                long totalLectureCount,
                long completedProblemCount,
                long totalProblemCount
        ) {
            boolean lecturesCompleted = totalLectureCount > 0 && completedLectureCount == totalLectureCount;
            boolean problemsCompleted = completedProblemCount == totalProblemCount;
            boolean learningCompleted = lecturesCompleted && problemsCompleted;
            int lectureProgressRate = totalLectureCount == 0
                    ? 0
                    : (int) ((completedLectureCount * 100) / totalLectureCount);

            return new EnrollmentLearningProgress(
                    learningCompleted,
                    learningCompleted ? "COMPLETED" : "IN_PROGRESS",
                    lectureProgressRate,
                    completedLectureCount,
                    totalLectureCount,
                    completedProblemCount,
                    totalProblemCount
            );
        }
    }
}
