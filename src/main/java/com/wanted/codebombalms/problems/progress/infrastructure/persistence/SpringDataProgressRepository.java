package com.wanted.codebombalms.problems.progress.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataProgressRepository extends JpaRepository<ProgressJpaEntity, Long> {

    Optional<ProgressJpaEntity> findByUserIdAndProblemSet_ProblemSetId(
            Long userId,
            Long problemSetId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select progress
            from ProgressJpaEntity progress
            where progress.userId = :userId
              and progress.problemSet.problemSetId = :problemSetId
            """)
    Optional<ProgressJpaEntity> findByUserIdAndProblemSetIdForUpdate(
            @Param("userId") Long userId,
            @Param("problemSetId") Long problemSetId
    );
}
