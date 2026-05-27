package com.wanted.codebombalms.learning.application.usecase;

public interface LectureProblemSetQueryUseCase {

    LectureProblemSetEntryView enterLectureProblemSet(Long userId, Long lectureProblemSetId);

    LectureProblemSetProgressView findLectureProblemSetProgress(Long userId, Long lectureProblemSetId);

    record LectureProblemSetEntryView(
            Long lectureProblemSetId,
            Long problemSetId,
            String title,
            String description,
            Integer currentProblemNumber,
            Boolean completed,
            ProblemDetailView problem
    ) {
    }

    record ProblemDetailView(
            Long problemId,
            Integer problemNumber,
            String title,
            String content,
            String problemType,
            Integer point,
            String startCode
    ) {
    }

    record LectureProblemSetProgressView(
            Long lectureProblemSetId,
            Long problemSetId,
            Integer totalProblemCount,
            Integer currentProblemNumber,
            Long currentProblemId,
            Integer solvedProblemCount,
            Boolean completed,
            java.util.List<ProblemProgressItemView> problems
    ) {
    }

    record ProblemProgressItemView(
            Long problemId,
            Integer problemNumber,
            String status
    ) {
    }
}
