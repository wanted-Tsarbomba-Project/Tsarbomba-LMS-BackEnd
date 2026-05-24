package com.wanted.codebombalms.course.presentation.api.response;

import com.wanted.codebombalms.course.domain.model.CourseProblemStep;

public record CourseProblemStepResponse(
        Long courseProblemStepId,
        Long courseProblemSetId,
        Long problemId,
        Long lectureId,
        Long stepOrder
) {

    public static CourseProblemStepResponse from(CourseProblemStep courseProblemStep) {
        return new CourseProblemStepResponse(
                courseProblemStep.getCourseProblemStepId(),
                courseProblemStep.getCourseProblemSetId(),
                courseProblemStep.getProblemId(),
                courseProblemStep.getLectureId(),
                courseProblemStep.getStepOrder()
        );
    }
}
