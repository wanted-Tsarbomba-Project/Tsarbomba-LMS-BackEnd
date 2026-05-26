package com.wanted.codebombalms.learning.domain.model;

import java.time.LocalDateTime;

public record LectureProblemSubmission(
        Long lectureProblemSubmissionId,
        Long userId,
        Long courseProblemStepId,
        Long problemId,
        String submittedAnswer,
        Boolean correct,
        Integer attemptNo,
        LocalDateTime submittedAt
) {

    public static LectureProblemSubmission submit(
            Long userId,
            Long courseProblemStepId,
            Long problemId,
            String submittedAnswer,
            boolean correct,
            Integer attemptNo
    ) {
        return new LectureProblemSubmission(
                null,
                userId,
                courseProblemStepId,
                problemId,
                submittedAnswer,
                correct,
                attemptNo,
                LocalDateTime.now()
        );
    }
}