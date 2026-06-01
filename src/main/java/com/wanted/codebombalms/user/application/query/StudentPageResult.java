package com.wanted.codebombalms.user.application.query;

import java.util.List;

public record StudentPageResult(
        List<StudentSummary> content,
        long totalElements,
        int totalPages
) {
}
