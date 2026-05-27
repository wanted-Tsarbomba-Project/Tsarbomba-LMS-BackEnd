package com.wanted.codebombalms.user.presentation.api.dto.response;

import com.wanted.codebombalms.user.application.query.StudentPageResult;

import java.util.List;

public record StudentPageResponse(
        List<StudentSummaryResponse> content,
        long totalElements,
        int totalPages
) {

    public static StudentPageResponse from(StudentPageResult result) {
        List<StudentSummaryResponse> content = result.content().stream()
                .map(StudentSummaryResponse::from)
                .toList();
        return new StudentPageResponse(content, result.totalElements(), result.totalPages());
    }
}
