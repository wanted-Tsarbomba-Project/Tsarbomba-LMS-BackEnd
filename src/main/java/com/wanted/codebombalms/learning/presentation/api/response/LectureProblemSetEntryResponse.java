package com.wanted.codebombalms.learning.presentation.api.response;

import com.wanted.codebombalms.learning.application.usecase.LectureProblemSetQueryUseCase.LectureProblemSetEntryView;

public record LectureProblemSetEntryResponse(
        Long lectureProblemSetId,
        Long problemSetId,
        String title,
        String description,
        Integer currentProblemNumber,
        Boolean completed,
        ProblemDetailResponse problem
) {

    public static LectureProblemSetEntryResponse from(LectureProblemSetEntryView entry) {
        return new LectureProblemSetEntryResponse(
                entry.lectureProblemSetId(),
                entry.problemSetId(),
                entry.title(),
                entry.description(),
                entry.currentProblemNumber(),
                entry.completed(),
                entry.problem() == null ? null : ProblemDetailResponse.from(entry.problem())
        );
    }

    public record ProblemDetailResponse(
            Long problemId,
            Integer problemNumber,
            String title,
            String content,
            String problemType,
            Integer point,
            String startCode
    ) {

        private static ProblemDetailResponse from(
                com.wanted.codebombalms.learning.application.usecase.LectureProblemSetQueryUseCase.ProblemDetailView problem
        ) {
            return new ProblemDetailResponse(
                    problem.problemId(),
                    problem.problemNumber(),
                    problem.title(),
                    problem.content(),
                    problem.problemType(),
                    problem.point(),
                    problem.startCode()
            );
        }
    }
}
