package com.wanted.codebombalms.learning.application.command;

public record RecordLectureProblemProgressCommand(
        Long userId,
        Long lectureProblemSetId,
        Integer currentProblemNumber,
        boolean completed
) {
}
