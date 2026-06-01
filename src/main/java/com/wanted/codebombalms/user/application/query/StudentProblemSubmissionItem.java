package com.wanted.codebombalms.user.application.query;

import java.time.LocalDateTime;

public record StudentProblemSubmissionItem(
        Long problemSetId,
        String problemSetTitle,
        String problemSetDescription,
        String problemSetDifficulty,
        Integer totalProblemCount,

        Long problemId,
        String problemTitle,
        String problemType,
        String problemDifficulty,
        Integer problemOrder,
        Integer point,
        Integer attemptLimit,
        Boolean retriable,

        Long submissionId,
        String submittedAnswer,
        String submittedCode,
        Boolean correct,
        Integer earnedPoint,
        Integer attemptNo,
        LocalDateTime submittedAt,
        String submissionStatus
) {
}
