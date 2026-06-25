package com.wanted.codebombalms.learning.presentation.api.response;

import com.wanted.codebombalms.learning.domain.model.StudentLearningProgressPage;
import java.util.List;

public record StudentLearningProgressPageResponse(
        List<StudentLearningProgressResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static StudentLearningProgressPageResponse from(StudentLearningProgressPage page) {
        return new StudentLearningProgressPageResponse(
                page.content()
                        .stream()
                        .map(StudentLearningProgressResponse::from)
                        .toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.hasNext()
        );
    }
}
