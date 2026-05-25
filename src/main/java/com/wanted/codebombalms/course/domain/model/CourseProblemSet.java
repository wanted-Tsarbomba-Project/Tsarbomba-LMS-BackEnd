package com.wanted.codebombalms.course.domain.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CourseProblemSet {

    private final Long courseProblemSetId;
    private final Long courseId;
    private final Long problemSetId;
    private final CourseProblemSetRole role;

    private CourseProblemSet(
            Long courseProblemSetId,
            Long courseId,
            Long problemSetId,
            CourseProblemSetRole role
    ) {
        this.courseProblemSetId = courseProblemSetId;
        this.courseId = courseId;
        this.problemSetId = problemSetId;
        this.role = role;
    }

    public static CourseProblemSet create(Long courseId, Long problemSetId, CourseProblemSetRole role) {
        return new CourseProblemSet(null, courseId, problemSetId, role);
    }

    public static CourseProblemSet restore(
            Long courseProblemSetId,
            Long courseId,
            Long problemSetId,
            CourseProblemSetRole role
    ) {
        return new CourseProblemSet(courseProblemSetId, courseId, problemSetId, role);
    }
}
