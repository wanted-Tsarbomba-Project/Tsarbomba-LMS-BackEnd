package com.wanted.codebombalms.problems.set.application.query;

public record GetProblemSetsQuery(
        Long categoryId,
        int page,
        int size,
        String sort
) {
    private static final String DEFAULT_SORT = "default";
    private static final String POPULAR_SORT = "popular";

    public String normalizedSort() {
        if (sort == null || sort.isBlank()) {
            return DEFAULT_SORT;
        }

        return sort.trim().toLowerCase();
    }

    public boolean isDefaultSort() {
        return DEFAULT_SORT.equals(normalizedSort());
    }

    public boolean isPopularSort() {
        return POPULAR_SORT.equals(normalizedSort());
    }
}
