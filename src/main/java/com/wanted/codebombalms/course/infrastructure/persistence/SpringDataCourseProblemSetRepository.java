package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCourseProblemSetRepository extends JpaRepository<CourseProblemSetJpaEntity, Long> {

    List<CourseProblemSetJpaEntity> findByCourse_CourseId(Long courseId);

    List<CourseProblemSetJpaEntity> findByCourse_CourseIdAndRole(Long courseId, CourseProblemSetRole role);
}
