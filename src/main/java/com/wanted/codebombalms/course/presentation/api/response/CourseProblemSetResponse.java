package com.wanted.codebombalms.course.presentation.api.response;

import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;

public record CourseProblemSetResponse(
        Long courseProblemSetId,
        Long courseId,
        Long lectureId,
        Long problemSetId,
        CourseProblemSetRole role,
        Integer displayOrder
) {

    public static CourseProblemSetResponse from(CourseProblemSet courseProblemSet) {
        return new CourseProblemSetResponse(
                courseProblemSet.getCourseProblemSetId(),
                courseProblemSet.getCourseId(),
                courseProblemSet.getLectureId(),
                courseProblemSet.getProblemSetId(),
                courseProblemSet.getRole(),
                courseProblemSet.getDisplayOrder()
        );
    }
}
