package com.wanted.codebombalms.learning.presentation.api.response;

import com.wanted.codebombalms.learning.application.usecase.LectureProblemSetQueryUseCase.LectureProblemSetProgressView;
import java.util.List;

public record LectureProblemSetProgressResponse(
        Long lectureProblemSetId,
        Long problemSetId,
        Integer totalProblemCount,
        Integer currentProblemNumber,
        Long currentProblemId,
        Integer solvedProblemCount,
        Boolean completed,
        List<ProblemProgressItemResponse> problems
) {

    public static LectureProblemSetProgressResponse from(LectureProblemSetProgressView progress) {
        return new LectureProblemSetProgressResponse(
                progress.lectureProblemSetId(),
                progress.problemSetId(),
                progress.totalProblemCount(),
                progress.currentProblemNumber(),
                progress.currentProblemId(),
                progress.solvedProblemCount(),
                progress.completed(),
                progress.problems()
                        .stream()
                        .map(ProblemProgressItemResponse::from)
                        .toList()
        );
    }

    public record ProblemProgressItemResponse(
            Long problemId,
            Integer problemNumber,
            String status
    ) {

        private static ProblemProgressItemResponse from(
                com.wanted.codebombalms.learning.application.usecase.LectureProblemSetQueryUseCase.ProblemProgressItemView item
        ) {
            return new ProblemProgressItemResponse(
                    item.problemId(),
                    item.problemNumber(),
                    item.status()
            );
        }
    }
}
