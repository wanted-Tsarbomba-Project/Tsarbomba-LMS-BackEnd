package com.wanted.codebombalms.user.application.query;

public record StudentProblemSubmissionQuery(
        Long userId,
        Long problemSetId,
        Long problemId,
        Boolean correctOnly
) {
    public boolean correctOnlyValue() {
        return Boolean.TRUE.equals(correctOnly);
    }
}
