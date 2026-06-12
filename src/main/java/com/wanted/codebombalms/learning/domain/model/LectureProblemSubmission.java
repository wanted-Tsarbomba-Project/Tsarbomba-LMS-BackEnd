package com.wanted.codebombalms.learning.domain.model;

import java.time.LocalDateTime;

public record LectureProblemSubmission(
        Long lectureProblemSubmissionId,
        Long userId,
        Long lectureProblemSetId,
        Long problemId,
        String submittedCode,
        boolean correct,
        Integer attemptNo,
        Integer passedTestCount,
        Integer totalTestCount,
        String executionStatus,
        String errorMessage,
        LocalDateTime submittedAt
) {

    public static LectureProblemSubmission create(
            Long userId,
            Long lectureProblemSetId,
            Long problemId,
            String submittedCode,
            boolean correct,
            Integer attemptNo,
            Integer passedTestCount,
            Integer totalTestCount,
            String executionStatus,
            String errorMessage
    ) {
        return new LectureProblemSubmission(
                null,
                userId,
                lectureProblemSetId,
                problemId,
                submittedCode,
                correct,
                attemptNo,
                passedTestCount,
                totalTestCount,
                executionStatus,
                errorMessage,
                null
        );
    }
}
