package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.CourseProblemStep;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "course_problem_step")
public class CourseProblemStepJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_problem_step_id")
    private Long courseProblemStepId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_problem_set_id", nullable = false)
    private CourseProblemSetJpaEntity courseProblemSet;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "lecture_id")
    private Long lectureId;

    @Column(name = "step_order", nullable = false)
    private Long stepOrder;

    private CourseProblemStepJpaEntity(
            CourseProblemSetJpaEntity courseProblemSet,
            Long problemId,
            Long lectureId,
            Long stepOrder
    ) {
        this.courseProblemSet = courseProblemSet;
        this.problemId = problemId;
        this.lectureId = lectureId;
        this.stepOrder = stepOrder;
    }

    public static CourseProblemStepJpaEntity from(
            CourseProblemStep courseProblemStep,
            CourseProblemSetJpaEntity courseProblemSet
    ) {
        CourseProblemStepJpaEntity entity = new CourseProblemStepJpaEntity(
                courseProblemSet,
                courseProblemStep.getProblemId(),
                courseProblemStep.getLectureId(),
                courseProblemStep.getStepOrder()
        );
        entity.courseProblemStepId = courseProblemStep.getCourseProblemStepId();
        return entity;
    }

    public void apply(CourseProblemStep courseProblemStep, CourseProblemSetJpaEntity courseProblemSet) {
        this.courseProblemSet = courseProblemSet;
        this.problemId = courseProblemStep.getProblemId();
        this.lectureId = courseProblemStep.getLectureId();
        this.stepOrder = courseProblemStep.getStepOrder();
    }

    public CourseProblemStep toDomain() {
        return CourseProblemStep.restore(
                courseProblemStepId,
                courseProblemSet.getCourseProblemSetId(),
                problemId,
                lectureId,
                stepOrder
        );
    }
}
