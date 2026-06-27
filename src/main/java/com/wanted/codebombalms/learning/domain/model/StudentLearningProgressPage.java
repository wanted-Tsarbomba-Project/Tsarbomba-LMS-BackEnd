package com.wanted.codebombalms.learning.domain.model;

import java.util.List;

public record StudentLearningProgressPage(
        List<StudentLearningProgress> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static StudentLearningProgressPage of(
            List<StudentLearningProgress> content,
            int page,
            int size,
            long totalElements
    ) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page + 1 < totalPages;

        return new StudentLearningProgressPage(
                content,
                page,
                size,
                totalElements,
                totalPages,
                hasNext
        );
    }
}
