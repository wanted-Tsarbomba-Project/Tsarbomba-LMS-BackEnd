package com.wanted.codebombalms.problems.result.presentation.api.response;

import com.wanted.codebombalms.problems.result.application.usecase.GetProblemSetResultUseCase.ProblemSubmissionResultView;

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
    public ProblemSubmissionResultResponse(ProblemSubmissionResultView submission) {
        this(
                submission.problemId(),
                submission.problemNumber(),
                submission.title(),
                submission.content(),
                submission.submittedAnswer(),
                submission.isCorrect(),
                submission.submittedAt(),
                submission.explanation()
        );
    }
}
