package com.wanted.codebombalms.learning.infrastructure.persistence;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataLectureProblemProgressRepository extends JpaRepository<LectureProblemProgressJpaEntity, Long> {

    Optional<LectureProblemProgressJpaEntity> findByUserIdAndCourseProblemStepId(
            Long userId,
            Long courseProblemStepId
    );

    long countByUserIdAndCourseProblemStepIdInAndCompletedTrue(
            Long userId,
            Collection<Long> courseProblemStepIds
    );
}
