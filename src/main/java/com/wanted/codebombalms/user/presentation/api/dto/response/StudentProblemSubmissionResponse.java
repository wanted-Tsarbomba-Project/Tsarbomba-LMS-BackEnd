package com.wanted.codebombalms.user.presentation.api.dto.response;

import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionItem;

import java.time.LocalDateTime;

public record StudentProblemSubmissionResponse(
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
        Boolean isRetriable,

        Long submissionId,
        String submittedAnswer,
        String submittedCode,
        Boolean isCorrect,
        Integer earnedPoint,
        Integer attemptNo,
        LocalDateTime submittedAt,
        String submissionStatus
) {
    public static StudentProblemSubmissionResponse from(StudentProblemSubmissionItem item) {
        return new StudentProblemSubmissionResponse(
                item.problemSetId(),
                item.problemSetTitle(),
                item.problemSetDescription(),
                item.problemSetDifficulty(),
                item.totalProblemCount(),

                item.problemId(),
                item.problemTitle(),
                item.problemType(),
                item.problemDifficulty(),
                item.problemOrder(),
                item.point(),
                item.attemptLimit(),
                item.retriable(),

                item.submissionId(),
                item.submittedAnswer(),
                item.submittedCode(),
                item.correct(),
                item.earnedPoint(),
                item.attemptNo(),
                item.submittedAt(),
                item.submissionStatus()
        );
    }
}
