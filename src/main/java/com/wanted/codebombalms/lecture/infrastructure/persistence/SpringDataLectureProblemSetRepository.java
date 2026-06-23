package com.wanted.codebombalms.lecture.infrastructure.persistence;

import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataLectureProblemSetRepository extends JpaRepository<LectureProblemSetJpaEntity, Long> {

    @Query("""
            select cps
            from LectureProblemSetJpaEntity cps
            where cps.courseId = :courseId
              and exists (
                  select c.courseId
                  from CourseJpaEntity c
                  where c.courseId = cps.courseId
                    and c.deletedAt is null
              )
              and (cps.lectureId is null or exists (
                  select l.lectureId
                  from LectureJpaEntity l
                  where l.lectureId = cps.lectureId
                    and l.deletedAt is null
              ))
              and exists (
                  select ps.problemSetId
                  from ProblemSetJpaEntity ps
                  where ps.problemSetId = cps.problemSetId
                    and ps.deletedAt is null
              )
            order by cps.displayOrder asc, cps.lectureProblemSetId asc
            """)
    List<LectureProblemSetJpaEntity> findActiveByCourseId(@Param("courseId") Long courseId);

    @Query("""
            select cps
            from LectureProblemSetJpaEntity cps
            where cps.courseId = :courseId
              and cps.role = :role
              and exists (
                  select c.courseId
                  from CourseJpaEntity c
                  where c.courseId = cps.courseId
                    and c.deletedAt is null
              )
              and (cps.lectureId is null or exists (
                  select l.lectureId
                  from LectureJpaEntity l
                  where l.lectureId = cps.lectureId
                    and l.deletedAt is null
              ))
              and exists (
                  select ps.problemSetId
                  from ProblemSetJpaEntity ps
                  where ps.problemSetId = cps.problemSetId
                    and ps.deletedAt is null
              )
            order by cps.displayOrder asc, cps.lectureProblemSetId asc
            """)
    List<LectureProblemSetJpaEntity> findActiveByCourseIdAndRole(
            @Param("courseId") Long courseId,
            @Param("role") LectureProblemSetRole role
    );

    @Query("""
            select cps
            from LectureProblemSetJpaEntity cps
            where cps.lectureId = :lectureId
              and exists (
                  select c.courseId
                  from CourseJpaEntity c
                  where c.courseId = cps.courseId
                    and c.deletedAt is null
              )
              and exists (
                  select l.lectureId
                  from LectureJpaEntity l
                  where l.lectureId = cps.lectureId
                    and l.deletedAt is null
              )
              and exists (
                  select ps.problemSetId
                  from ProblemSetJpaEntity ps
                  where ps.problemSetId = cps.problemSetId
                    and ps.deletedAt is null
              )
            order by cps.displayOrder asc, cps.lectureProblemSetId asc
            """)
    List<LectureProblemSetJpaEntity> findActiveByLectureIdOrderByDisplayOrderAsc(@Param("lectureId") Long lectureId);

    @Query("""
            select cps
            from LectureProblemSetJpaEntity cps
            where cps.lectureProblemSetId = :lectureProblemSetId
              and exists (
                  select c.courseId
                  from CourseJpaEntity c
                  where c.courseId = cps.courseId
                    and c.deletedAt is null
              )
              and (cps.lectureId is null or exists (
                  select l.lectureId
                  from LectureJpaEntity l
                  where l.lectureId = cps.lectureId
                    and l.deletedAt is null
              ))
              and exists (
                  select ps.problemSetId
                  from ProblemSetJpaEntity ps
                  where ps.problemSetId = cps.problemSetId
                    and ps.deletedAt is null
              )
            """)
    Optional<LectureProblemSetJpaEntity> findActiveById(@Param("lectureProblemSetId") Long lectureProblemSetId);
}
