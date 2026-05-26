package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "lecture_problem_set")
public class CourseProblemSetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_problem_set_id")
    private Long courseProblemSetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseJpaEntity course;

    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @Column(name = "problem_set_id", nullable = false)
    private Long problemSetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private CourseProblemSetRole role;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    private CourseProblemSetJpaEntity(
            CourseJpaEntity course,
            Long lectureId,
            Long problemSetId,
            CourseProblemSetRole role,
            Integer displayOrder
    ) {
        this.course = course;
        this.lectureId = lectureId;
        this.problemSetId = problemSetId;
        this.role = role;
        this.displayOrder = displayOrder;
    }

    public static CourseProblemSetJpaEntity from(CourseProblemSet courseProblemSet, CourseJpaEntity course) {
        CourseProblemSetJpaEntity entity = new CourseProblemSetJpaEntity(
                course,
                courseProblemSet.getLectureId(),
                courseProblemSet.getProblemSetId(),
                courseProblemSet.getRole(),
                courseProblemSet.getDisplayOrder()
        );
        entity.courseProblemSetId = courseProblemSet.getCourseProblemSetId();
        return entity;
    }

    public void apply(CourseProblemSet courseProblemSet, CourseJpaEntity course) {
        this.course = course;
        this.lectureId = courseProblemSet.getLectureId();
        this.problemSetId = courseProblemSet.getProblemSetId();
        this.role = courseProblemSet.getRole();
        this.displayOrder = courseProblemSet.getDisplayOrder();
    }

    public CourseProblemSet toDomain() {
        return CourseProblemSet.restore(
                courseProblemSetId,
                course.getCourseId(),
                lectureId,
                problemSetId,
                role,
                displayOrder
        );
    }
}
