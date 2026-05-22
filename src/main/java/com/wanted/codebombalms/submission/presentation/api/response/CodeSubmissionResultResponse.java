package com.wanted.codebombalms.submission.presentation.api.response;

import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionResultQueryUseCase.CodeSubmissionResultView;

import java.time.LocalDateTime;
import java.util.List;

public record CodeSubmissionResultResponse(
        Long submissionId,
        Long problemId,
        Boolean isCorrect,
        Integer earnedScore,
        Integer passedTestCount,
        Integer totalTestCount,
        String executionStatus,
        String errorMessage,
        LocalDateTime submittedAt,
        List<TestCaseResultResponse> testCaseResults
) {

    public CodeSubmissionResultResponse(CodeSubmissionResultView result) {
        this(
                result.submissionId(),
                result.problemId(),
                result.correct(),
                result.earnedScore(),
                result.passedTestCount(),
                result.totalTestCount(),
                result.executionStatus(),
                result.errorMessage(),
                result.submittedAt(),
                result.testCaseResults()
                        .stream()
                        .map(TestCaseResultResponse::new)
                        .toList()
        );
    }
}
