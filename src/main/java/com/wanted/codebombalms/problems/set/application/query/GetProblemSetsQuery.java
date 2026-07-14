package com.wanted.codebombalms.problems.set.application.query;

public record GetProblemSetsQuery(
        Long userId,
        Long categoryId,
        ProblemSetDifficulty difficulty,
        ProblemSetCompletionStatus completionStatus,
        int page,
        int size,
        ProblemSetSort sort,
        ProblemSetSortDirection direction
) {

    public boolean isPopularSort() {
        return sort != null && sort.isPopular();
    }

    public boolean hasCompletionStatusFilter() {
        return completionStatus != null;
    }

    public ProblemSetSortDirection resolvedDirection() {
        return direction == null ? ProblemSetSortDirection.ASC : direction;
    }
    public String difficultyName() {
        return difficulty == null ? null : difficulty.name();
    }
}
