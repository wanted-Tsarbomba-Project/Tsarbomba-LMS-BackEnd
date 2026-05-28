package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SpringDataCourseProblemSetRepository extends JpaRepository<CourseProblemSetJpaEntity, Long> {

    List<CourseProblemSetJpaEntity> findByCourse_CourseIdAndDeletedAtIsNull(Long courseId);

    List<CourseProblemSetJpaEntity> findByCourse_CourseIdAndRoleAndDeletedAtIsNull(
            Long courseId,
            CourseProblemSetRole role
    );

    List<CourseProblemSetJpaEntity> findByLectureIdAndDeletedAtIsNullOrderByDisplayOrderAsc(Long lectureId);

    Optional<CourseProblemSetJpaEntity> findByCourseProblemSetIdAndDeletedAtIsNull(Long courseProblemSetId);

    @Transactional
    default int hardDeleteByDeletedAtBefore(LocalDateTime threshold) {
        List<Long> lectureProblemSetIds = findHardDeleteTargetIds(threshold);
        if (lectureProblemSetIds.isEmpty()) {
            return 0;
        }

        deleteLectureProblemProgressesByLectureProblemSetIds(lectureProblemSetIds);
        return deleteLectureProblemSetsByIds(lectureProblemSetIds);
    }

    @Query("""
            select cps.courseProblemSetId
            from CourseProblemSetJpaEntity cps
            where cps.deletedAt is not null
              and cps.deletedAt < :threshold
            """)
    List<Long> findHardDeleteTargetIds(@Param("threshold") LocalDateTime threshold);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from LectureProblemProgressJpaEntity p
            where p.lectureProblemSetId in :lectureProblemSetIds
            """)
    int deleteLectureProblemProgressesByLectureProblemSetIds(
            @Param("lectureProblemSetIds") List<Long> lectureProblemSetIds
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from CourseProblemSetJpaEntity cps
            where cps.courseProblemSetId in :lectureProblemSetIds
            """)
    int deleteLectureProblemSetsByIds(@Param("lectureProblemSetIds") List<Long> lectureProblemSetIds);
}
