package com.wanted.codebombalms.problems.result.presentation.api.response;

import com.wanted.codebombalms.problems.result.domain.model.ProblemSubmissionResult;

import java.time.LocalDateTime;

public record ProblemSubmissionResultResponse(
        Long problemId,
        Integer problemNumber,
        String title,
        String content,
        String submittedAnswer,
        Boolean isCorrect,
        LocalDateTime submittedAt,
        String explanation
) {
    public ProblemSubmissionResultResponse(ProblemSubmissionResult submission) {
        this(
                submission.getProblemId(),
                submission.getProblemNumber(),
                submission.getTitle(),
                submission.getContent(),
                submission.getSubmittedAnswer(),
                submission.getCorrect(),
                submission.getSubmittedAt(),
                submission.getExplanation()
        );
    }
}
