package com.wanted.codebombalms.learning.application.usecase;

import java.util.List;

public interface LectureProblemSetQueryUseCase {

    LectureProblemSetEntryView enterLectureProblemSet(Long userId, Long lectureProblemSetId);

    LectureProblemSetProgressView findLectureProblemSetProgress(Long userId, Long lectureProblemSetId);

    record LectureProblemSetEntryView(
            Long lectureProblemSetId,
            Long problemSetId,
            String title,
            String description,
            Integer currentProblemNumber,
            Long currentProblemId,
            Integer totalProblemCount,
            Integer solvedProblemCount,
            Boolean completed,
            List<ProblemDetailView> problems
    ) {
    }

    record ProblemDetailView(
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