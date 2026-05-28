package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataCourseProblemSetRepository extends JpaRepository<CourseProblemSetJpaEntity, Long> {

    @Query("""
            select cps
            from CourseProblemSetJpaEntity cps
            where cps.course.courseId = :courseId
              and cps.course.deletedAt is null
              and exists (
                  select l.lectureId
                  from LectureJpaEntity l
                  where l.lectureId = cps.lectureId
                    and l.deletedAt is null
              )
            """)
    List<CourseProblemSetJpaEntity> findActiveByCourseId(@Param("courseId") Long courseId);

    @Query("""
            select cps
            from CourseProblemSetJpaEntity cps
            where cps.course.courseId = :courseId
              and cps.role = :role
              and cps.course.deletedAt is null
              and exists (
                  select l.lectureId
                  from LectureJpaEntity l
                  where l.lectureId = cps.lectureId
                    and l.deletedAt is null
              )
            """)
    List<CourseProblemSetJpaEntity> findActiveByCourseIdAndRole(
            @Param("courseId") Long courseId,
            @Param("role") CourseProblemSetRole role
    );

    @Query("""
            select cps
            from CourseProblemSetJpaEntity cps
            where cps.lectureId = :lectureId
              and cps.course.deletedAt is null
              and exists (
                  select l.lectureId
                  from LectureJpaEntity l
                  where l.lectureId = cps.lectureId
                    and l.deletedAt is null
              )
            order by cps.displayOrder asc
            """)
    List<CourseProblemSetJpaEntity> findActiveByLectureIdOrderByDisplayOrderAsc(@Param("lectureId") Long lectureId);

    @Query("""
            select cps
            from CourseProblemSetJpaEntity cps
            where cps.courseProblemSetId = :courseProblemSetId
              and cps.course.deletedAt is null
              and exists (
                  select l.lectureId
                  from LectureJpaEntity l
                  where l.lectureId = cps.lectureId
                    and l.deletedAt is null
              )
            """)
    Optional<CourseProblemSetJpaEntity> findActiveById(@Param("courseProblemSetId") Long courseProblemSetId);
}
