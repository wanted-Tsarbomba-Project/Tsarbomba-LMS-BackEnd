package com.wanted.codebombalms.course.domain.model;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class CourseProblemSet {

    private Long courseProblemSetId;
    private Long courseId;
    private Long lectureId;
    private Long problemSetId;
    private CourseProblemSetRole role;
    private Integer displayOrder;
    private LocalDateTime deletedAt;

    private CourseProblemSet(
            Long courseProblemSetId,
            Long courseId,
            Long lectureId,
            Long problemSetId,
            CourseProblemSetRole role,
            Integer displayOrder,
            LocalDateTime deletedAt
    ) {
        this.courseProblemSetId = courseProblemSetId;
        this.courseId = courseId;
        this.lectureId = lectureId;
        this.problemSetId = problemSetId;
        this.role = role;
        this.displayOrder = displayOrder;
        this.deletedAt = deletedAt;
    }

    public static CourseProblemSet create(
            Long courseId,
            Long lectureId,
            Long problemSetId,
            CourseProblemSetRole role,
            Integer displayOrder
    ) {
        return new CourseProblemSet(null, courseId, lectureId, problemSetId, role, displayOrder, null);
    }

    public static CourseProblemSet restore(
            Long courseProblemSetId,
            Long courseId,
            Long lectureId,
            Long problemSetId,
            CourseProblemSetRole role,
            Integer displayOrder
    ) {
        return restore(courseProblemSetId, courseId, lectureId, problemSetId, role, displayOrder, null);
    }

    public static CourseProblemSet restore(
            Long courseProblemSetId,
            Long courseId,
            Long lectureId,
            Long problemSetId,
            CourseProblemSetRole role,
            Integer displayOrder,
            LocalDateTime deletedAt
    ) {
        return new CourseProblemSet(courseProblemSetId, courseId, lectureId, problemSetId, role, displayOrder, deletedAt);
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
