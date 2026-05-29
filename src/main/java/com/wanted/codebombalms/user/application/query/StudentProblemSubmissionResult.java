package com.wanted.codebombalms.user.application.query;

import java.util.List;

public record StudentProblemSubmissionResult(
        Long userId,
        Integer totalCount,
        List<StudentProblemSubmissionItem> submissions
) {
    public static StudentProblemSubmissionResult of(
            Long userId,
            long totalCount,
            List<StudentProblemSubmissionItem> submissions
    ) {
        return new StudentProblemSubmissionResult(
                userId,
                Math.toIntExact(totalCount),
                submissions
        );
    }
}
