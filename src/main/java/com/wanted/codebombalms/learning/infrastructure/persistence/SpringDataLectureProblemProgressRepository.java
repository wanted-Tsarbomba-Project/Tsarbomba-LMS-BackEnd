package com.wanted.codebombalms.learning.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataLectureProblemProgressRepository extends JpaRepository<LectureProblemProgressJpaEntity, Long> {

    Optional<LectureProblemProgressJpaEntity> findByUserIdAndLectureProblemSetId(
            Long userId,
            Long lectureProblemSetId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select progress
            from LectureProblemProgressJpaEntity progress
            where progress.userId = :userId
              and progress.lectureProblemSetId = :lectureProblemSetId
            """)
    Optional<LectureProblemProgressJpaEntity> findByUserIdAndLectureProblemSetIdForUpdate(
            @Param("userId") Long userId,
            @Param("lectureProblemSetId") Long lectureProblemSetId
    );

    long countByUserIdAndLectureProblemSetIdInAndCompletedTrue(
            Long userId,
            Collection<Long> lectureProblemSetIds
    );

    long countByLectureProblemSetIdInAndCompletedTrue(Collection<Long> lectureProblemSetIds);
}
