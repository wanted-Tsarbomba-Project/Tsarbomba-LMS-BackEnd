package com.wanted.codebombalms.lecture.infrastructure.persistence;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SpringDataLectureRepository extends JpaRepository<LectureJpaEntity, Long> {

    List<LectureJpaEntity> findByDeletedAtIsNull();

    Optional<LectureJpaEntity> findByLectureIdAndDeletedAtIsNull(Long lectureId);

    List<LectureJpaEntity> findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(Long courseId);

    List<LectureJpaEntity> findByCourseIdAndDeletedAtIsNull(Long courseId);

    boolean existsByCourseIdAndDeletedAtIsNull(Long courseId);

    boolean existsByCourseIdAndLectureIdAndDeletedAtIsNull(Long courseId, Long lectureId);

    @Transactional
    default int hardDeleteByDeletedAtBefore(LocalDateTime threshold) {
        List<Long> lectureIds = findHardDeleteTargetIds(threshold);
        if (lectureIds.isEmpty()) {
            return 0;
        }

        List<Long> lectureProblemSetIds = findLectureProblemSetIdsByLectureIds(lectureIds);
        if (!lectureProblemSetIds.isEmpty()) {
            deleteLectureProblemSubmissionsByLectureProblemSetIds(lectureProblemSetIds);
            deleteLectureProblemProgressesByLectureProblemSetIds(lectureProblemSetIds);
        }

        deleteLectureProblemSetsByLectureIds(lectureIds);
        deleteLectureProgressesByLectureIds(lectureIds);
        return deleteLecturesByIds(lectureIds);
    }

    @Query("""
            select l.lectureId
            from LectureJpaEntity l
            where l.deletedAt is not null
              and l.deletedAt < :threshold
            """)
    List<Long> findHardDeleteTargetIds(@Param("threshold") LocalDateTime threshold);

    @Query("""
            select cps.lectureProblemSetId
            from LectureProblemSetJpaEntity cps
            where cps.lectureId in :lectureIds
            """)
    List<Long> findLectureProblemSetIdsByLectureIds(@Param("lectureIds") List<Long> lectureIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from LectureProblemSubmissionJpaEntity s
            where s.lectureProblemSetId in :lectureProblemSetIds
            """)
    int deleteLectureProblemSubmissionsByLectureProblemSetIds(
            @Param("lectureProblemSetIds") List<Long> lectureProblemSetIds
    );

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
            delete from LectureProblemSetJpaEntity cps
            where cps.lectureId in :lectureIds
            """)
    int deleteLectureProblemSetsByLectureIds(@Param("lectureIds") List<Long> lectureIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from LectureProgressJpaEntity p
            where p.lectureId in :lectureIds
            """)
    int deleteLectureProgressesByLectureIds(@Param("lectureIds") List<Long> lectureIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from LectureJpaEntity l
            where l.lectureId in :lectureIds
            """)
    int deleteLecturesByIds(@Param("lectureIds") List<Long> lectureIds);
}
