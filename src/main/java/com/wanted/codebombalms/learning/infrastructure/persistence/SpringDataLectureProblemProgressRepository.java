package com.wanted.codebombalms.learning.infrastructure.persistence;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataLectureProblemProgressRepository extends JpaRepository<LectureProblemProgressJpaEntity, Long> {

    Optional<LectureProblemProgressJpaEntity> findByUserIdAndLectureProblemSetId(
            Long userId,
            Long lectureProblemSetId
    );

    long countByUserIdAndLectureProblemSetIdInAndCompletedTrue(
            Long userId,
            Collection<Long> lectureProblemSetIds
    );

    long countByLectureProblemSetIdInAndCompletedTrue(Collection<Long> lectureProblemSetIds);
}
