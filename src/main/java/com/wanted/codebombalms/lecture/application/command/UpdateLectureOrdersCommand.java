package com.wanted.codebombalms.lecture.application.command;

import java.util.List;

public record UpdateLectureOrdersCommand(
        Long courseId,
        List<LectureOrderItem> lectures
) {

    public record LectureOrderItem(
            Long lectureId,
            Integer lectureOrder
    ) {
    }
}
