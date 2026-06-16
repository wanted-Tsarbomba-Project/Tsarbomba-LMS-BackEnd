package com.wanted.codebombalms.lecture.infrastructure.persistence;

import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "lecture_problem_set")
public class LectureProblemSetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_problem_set_id")
    private Long lectureProblemSetId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "lecture_id")
    private Long lectureId;

    @Column(name = "problem_set_id", nullable = false)
    private Long problemSetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private LectureProblemSetRole role;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    private LectureProblemSetJpaEntity(
            Long courseId,
            Long lectureId,
            Long problemSetId,
            LectureProblemSetRole role,
            Integer displayOrder
    ) {
        this.courseId = courseId;
        this.lectureId = lectureId;
        this.problemSetId = problemSetId;
        this.role = role;
        this.displayOrder = displayOrder;
    }

    public static LectureProblemSetJpaEntity from(LectureProblemSet lectureProblemSet) {
        LectureProblemSetJpaEntity entity = new LectureProblemSetJpaEntity(
                lectureProblemSet.getCourseId(),
                lectureProblemSet.getLectureId(),
                lectureProblemSet.getProblemSetId(),
                lectureProblemSet.getRole(),
                lectureProblemSet.getDisplayOrder()
        );
        entity.lectureProblemSetId = lectureProblemSet.getLectureProblemSetId();
        return entity;
    }

    public void apply(LectureProblemSet lectureProblemSet) {
        this.courseId = lectureProblemSet.getCourseId();
        this.lectureId = lectureProblemSet.getLectureId();
        this.problemSetId = lectureProblemSet.getProblemSetId();
        this.role = lectureProblemSet.getRole();
        this.displayOrder = lectureProblemSet.getDisplayOrder();
    }

    public LectureProblemSet toDomain() {
        return LectureProblemSet.restore(
                lectureProblemSetId,
                courseId,
                lectureId,
                problemSetId,
                role,
                displayOrder
        );
    }
}
