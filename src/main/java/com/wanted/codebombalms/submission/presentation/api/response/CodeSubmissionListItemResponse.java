package com.wanted.codebombalms.submission.presentation.api.response;

import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionListQueryUseCase.CodeSubmissionListItemView;

import java.time.LocalDateTime;

public record CodeSubmissionListItemResponse(
        Long submissionId,
        Long problemId,
        Boolean isCorrect,
        Integer earnedScore,
        Integer passedTestCount,
        Integer totalTestCount,
        String executionStatus,
        LocalDateTime submittedAt
) {

    public CodeSubmissionListItemResponse(CodeSubmissionListItemView item) {
        this(
                item.submissionId(),
                item.problemId(),
                item.correct(),
                item.earnedScore(),
                item.passedTestCount(),
                item.totalTestCount(),
                item.executionStatus(),
                item.submittedAt()
        );
    }
}
