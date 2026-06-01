package com.wanted.codebombalms.learning.presentation.api.response;

import com.wanted.codebombalms.learning.application.usecase.LectureProblemSetQueryUseCase.LectureProblemSetEntryView;

import java.util.List;

public record LectureProblemSetEntryResponse(
        Long lectureProblemSetId,
        Long problemSetId,
        String title,
        String description,
        Integer currentProblemNumber,
        Long currentProblemId,
        Integer totalProblemCount,
        Integer solvedProblemCount,
        Boolean completed,
        List<ProblemDetailResponse> problems
) {

    public static LectureProblemSetEntryResponse from(LectureProblemSetEntryView entry) {
        return new LectureProblemSetEntryResponse(
                entry.lectureProblemSetId(),
                entry.problemSetId(),
                entry.title(),
                entry.description(),
                entry.currentProblemNumber(),
                entry.currentProblemId(),
                entry.totalProblemCount(),
                entry.solvedProblemCount(),
                entry.completed(),
                entry.problems()
                        .stream()
                        .map(ProblemDetailResponse::from)
                        .toList()
        );
    }

    public record ProblemDetailResponse(
            Long problemId,
            Integer problemNumber,
            String title,
            String content,
            String problemType,
            Integer point,
            String startCode,
            String status,
            Long latestSubmissionId
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
                    problem.startCode(),
                    problem.status(),
                    problem.latestSubmissionId()
            );
        }
    }
}