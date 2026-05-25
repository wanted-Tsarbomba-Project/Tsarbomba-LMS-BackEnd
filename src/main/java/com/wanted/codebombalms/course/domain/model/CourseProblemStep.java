package com.wanted.codebombalms.course.domain.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CourseProblemStep {

    private final Long courseProblemStepId;
    private final Long courseProblemSetId;
    private final Long problemId;
    private final Long lectureId;
    private final Long stepOrder;

    private CourseProblemStep(
            Long courseProblemStepId,
            Long courseProblemSetId,
            Long problemId,
            Long lectureId,
            Long stepOrder
    ) {
        this.courseProblemStepId = courseProblemStepId;
        this.courseProblemSetId = courseProblemSetId;
        this.problemId = problemId;
        this.lectureId = lectureId;
        this.stepOrder = stepOrder;
    }

    public static CourseProblemStep create(
            Long courseProblemSetId,
            Long problemId,
            Long lectureId,
            Long stepOrder
    ) {
        return new CourseProblemStep(null, courseProblemSetId, problemId, lectureId, stepOrder);
    }

    public static CourseProblemStep restore(
            Long courseProblemStepId,
            Long courseProblemSetId,
            Long problemId,
            Long lectureId,
            Long stepOrder
    ) {
        return new CourseProblemStep(courseProblemStepId, courseProblemSetId, problemId, lectureId, stepOrder);
    }
}
