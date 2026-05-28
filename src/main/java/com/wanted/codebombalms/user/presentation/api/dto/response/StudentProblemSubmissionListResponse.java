package com.wanted.codebombalms.user.presentation.api.dto.response;

import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionResult;

import java.util.List;

public record StudentProblemSubmissionListResponse(
        Long userId,
        Integer totalCount,
        List<StudentProblemSubmissionResponse> submissions
) {
    public static StudentProblemSubmissionListResponse from(StudentProblemSubmissionResult result) {
        return new StudentProblemSubmissionListResponse(
                result.userId(),
                result.totalCount(),
                result.submissions().stream()
                        .map(StudentProblemSubmissionResponse::from)
                        .toList()
        );
    }
}
