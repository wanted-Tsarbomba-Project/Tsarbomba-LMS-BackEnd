package com.wanted.codebombalms.course.presentation.api.response;

import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;

public record CourseProblemSetResponse(
        Long courseProblemSetId,
        Long courseId,
        Long lectureId,
        Long problemSetId,
        LectureProblemSetRole role,
        Integer displayOrder
) {

    public static CourseProblemSetResponse from(LectureProblemSet lectureProblemSet) {
        return new CourseProblemSetResponse(
                lectureProblemSet.getLectureProblemSetId(),
                lectureProblemSet.getCourseId(),
                lectureProblemSet.getLectureId(),
                lectureProblemSet.getProblemSetId(),
                lectureProblemSet.getRole(),
                lectureProblemSet.getDisplayOrder()
        );
    }
}
