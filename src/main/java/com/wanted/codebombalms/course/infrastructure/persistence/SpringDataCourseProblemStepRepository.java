package com.wanted.codebombalms.course.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCourseProblemStepRepository extends JpaRepository<CourseProblemStepJpaEntity, Long> {

    List<CourseProblemStepJpaEntity> findByLectureIdOrderByStepOrderAsc(Long lectureId);

    List<CourseProblemStepJpaEntity> findByCourseProblemSet_CourseProblemSetIdOrderByStepOrderAsc(
            Long courseProblemSetId
    );
}
