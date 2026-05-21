package com.wanted.codebombalms.problems.result.application.usecase;

import com.wanted.codebombalms.problems.result.application.query.GetProblemSetResultQuery;

import java.time.LocalDateTime;
import java.util.List;

public interface GetProblemSetResultUseCase {

    ProblemSetResultView handle(GetProblemSetResultQuery query);

    record ProblemSetResultView(
            Long problemSetId,
            String title,
            Boolean isCompleted,
            Double accuracyRate,
            Integer totalCompletedUserCount,
            Integer correctCompletedUserCount,
            List<ProblemSubmissionResultView> submissions
    ) {
    }

    record ProblemSubmissionResultView(
            Long problemId,
            Integer problemNumber,
            String title,
            String content,
            String submittedAnswer,
            Boolean isCorrect,
            LocalDateTime submittedAt,
            String explanation
    ) {
    }
}
