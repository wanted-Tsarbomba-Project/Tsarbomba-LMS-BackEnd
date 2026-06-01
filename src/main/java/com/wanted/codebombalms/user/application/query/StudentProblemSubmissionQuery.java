package com.wanted.codebombalms.user.application.query;

public record StudentProblemSubmissionQuery(
        Long userId,
        Long problemSetId,
        Long problemId,
        Boolean correctOnly,
        Integer page,
        Integer size
) {
    public boolean correctOnlyValue() {
        return Boolean.TRUE.equals(correctOnly);
    }

    public int safePage() {
        if (page == null || page < 0) {
            return 0;
        }

        return page;
    }

    public int safeSize() {
        if (size == null || size < 1) {
            return 20;
        }

        return Math.min(size, 100);
    }

    public int offset() {
        return safePage() * safeSize();
    }
}
