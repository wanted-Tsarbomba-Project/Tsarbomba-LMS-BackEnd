package com.wanted.codebombalms.course.domain.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CourseProblemSet {

    private Long courseProblemSetId;
    private Long courseId;
    private Long lectureId;
    private Long problemSetId;
    private CourseProblemSetRole role;
    private Integer displayOrder;

    private CourseProblemSet(
            Long courseProblemSetId,
            Long courseId,
            Long lectureId,
            Long problemSetId,
            CourseProblemSetRole role,
            Integer displayOrder
    ) {
        this.courseProblemSetId = courseProblemSetId;
        this.courseId = courseId;
        this.lectureId = lectureId;
        this.problemSetId = problemSetId;
        this.role = role;
        this.displayOrder = displayOrder;
    }

    public static CourseProblemSet create(
            Long courseId,
            Long lectureId,
            Long problemSetId,
            CourseProblemSetRole role,
            Integer displayOrder
    ) {
        return new CourseProblemSet(null, courseId, lectureId, problemSetId, role, displayOrder);
    }

    public static CourseProblemSet restore(
            Long courseProblemSetId,
            Long courseId,
            Long lectureId,
            Long problemSetId,
            CourseProblemSetRole role,
            Integer displayOrder
    ) {
        return new CourseProblemSet(courseProblemSetId, courseId, lectureId, problemSetId, role, displayOrder);
    }
}
