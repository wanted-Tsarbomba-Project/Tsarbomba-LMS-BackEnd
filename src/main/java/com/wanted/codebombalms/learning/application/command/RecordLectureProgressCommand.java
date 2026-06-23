package com.wanted.codebombalms.learning.application.command;

public record RecordLectureProgressCommand(
        Long userId,
        Long lectureId,
        int lastPositionSec,
        Integer durationSec,
        int watchedDeltaSec
) {
}
