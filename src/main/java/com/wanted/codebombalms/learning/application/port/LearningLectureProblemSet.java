package com.wanted.codebombalms.learning.application.port;

public record LearningLectureProblemSet(
        Long lectureProblemSetId,
        Long courseId,
        Long lectureId,
        Long problemSetId
) {
}
