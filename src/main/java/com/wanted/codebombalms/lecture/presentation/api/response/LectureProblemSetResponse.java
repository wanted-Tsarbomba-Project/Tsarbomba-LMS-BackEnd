package com.wanted.codebombalms.lecture.presentation.api.response;

import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;

public record LectureProblemSetResponse(
        Long lectureProblemSetId,
        Long courseId,
        Long lectureId,
        Long problemSetId,
        LectureProblemSetRole role,
        Integer displayOrder
) {

    public static LectureProblemSetResponse from(LectureProblemSet lectureProblemSet) {
        return new LectureProblemSetResponse(
                lectureProblemSet.getLectureProblemSetId(),
                lectureProblemSet.getCourseId(),
                lectureProblemSet.getLectureId(),
                lectureProblemSet.getProblemSetId(),
                lectureProblemSet.getRole(),
                lectureProblemSet.getDisplayOrder()
        );
    }
}
