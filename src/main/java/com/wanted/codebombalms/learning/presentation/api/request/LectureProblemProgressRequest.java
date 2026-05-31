package com.wanted.codebombalms.learning.presentation.api.request;

public record LectureProblemProgressRequest(
        Integer currentProblemNumber,
        boolean completed
) {
}
