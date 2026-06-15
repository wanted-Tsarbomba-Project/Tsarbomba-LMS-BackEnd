package com.wanted.codebombalms.lecture.domain.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LectureProblemSet {

    private Long lectureProblemSetId;
    private Long courseId;
    private Long lectureId;
    private Long problemSetId;
    private LectureProblemSetRole role;
    private Integer displayOrder;

    private LectureProblemSet(
            Long lectureProblemSetId,
            Long courseId,
            Long lectureId,
            Long problemSetId,
            LectureProblemSetRole role,
            Integer displayOrder
    ) {
        this.lectureProblemSetId = lectureProblemSetId;
        this.courseId = courseId;
        this.lectureId = lectureId;
        this.problemSetId = problemSetId;
        this.role = role;
        this.displayOrder = displayOrder;
    }

    public static LectureProblemSet create(
            Long courseId,
            Long lectureId,
            Long problemSetId,
            LectureProblemSetRole role,
            Integer displayOrder
    ) {
        return new LectureProblemSet(null, courseId, lectureId, problemSetId, role, displayOrder);
    }

    public static LectureProblemSet restore(
            Long lectureProblemSetId,
            Long courseId,
            Long lectureId,
            Long problemSetId,
            LectureProblemSetRole role,
            Integer displayOrder
    ) {
        return new LectureProblemSet(lectureProblemSetId, courseId, lectureId, problemSetId, role, displayOrder);
    }
}
