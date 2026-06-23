package com.wanted.codebombalms.learning.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataLectureProgressRepository extends JpaRepository<LectureProgressJpaEntity, Long> {

    interface UserCompletedLectureCount {
        Long getUserId();

        long getCompletedCount();
    }

    Optional<LectureProgressJpaEntity> findByUserIdAndLectureId(Long userId, Long lectureId);

    long countByUserIdAndLectureIdInAndCompletedTrue(Long userId, Collection<Long> lectureIds);

    @Query("""
            select progress.userId as userId, count(progress) as completedCount
            from LectureProgressJpaEntity progress
            where progress.userId in :userIds
              and progress.lectureId in :lectureIds
              and progress.completed = true
            group by progress.userId
            """)
    List<UserCompletedLectureCount> countCompletedByUserIdsAndLectureIds(
            @Param("userIds") Collection<Long> userIds,
            @Param("lectureIds") Collection<Long> lectureIds
    );

    long countByLectureIdAndCompletedTrue(Long lectureId);
}
