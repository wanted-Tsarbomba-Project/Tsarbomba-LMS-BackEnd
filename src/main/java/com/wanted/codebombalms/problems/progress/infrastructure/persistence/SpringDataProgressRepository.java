package com.wanted.codebombalms.problems.progress.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProgressRepository extends JpaRepository<ProgressJpaEntity, Long> {

    Optional<ProgressJpaEntity> findByUserIdAndProblemSet_ProblemSetId(
            Long userId,
            Long problemSetId
    );
}
