package com.wanted.codebombalms.learning.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataLectureProblemProgressRepository extends JpaRepository<LectureProblemProgressJpaEntity, Long> {

    interface UserCompletedProblemSetCount {
        Long getUserId();

        long getCompletedCount();
    }

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

    @Query("""
            select progress.userId as userId, count(progress) as completedCount
            from LectureProblemProgressJpaEntity progress
            where progress.userId in :userIds
              and progress.lectureProblemSetId in :lectureProblemSetIds
              and progress.completed = true
            group by progress.userId
            """)
    List<UserCompletedProblemSetCount> countCompletedByUserIdsAndLectureProblemSetIds(
            @Param("userIds") Collection<Long> userIds,
            @Param("lectureProblemSetIds") Collection<Long> lectureProblemSetIds
    );

    long countByLectureProblemSetIdInAndCompletedTrue(Collection<Long> lectureProblemSetIds);
}
