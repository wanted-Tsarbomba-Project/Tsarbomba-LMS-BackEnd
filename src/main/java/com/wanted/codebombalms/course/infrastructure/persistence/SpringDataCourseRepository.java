package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.CourseStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SpringDataCourseRepository extends JpaRepository<CourseJpaEntity, Long> {

    List<CourseJpaEntity> findByDeletedAtIsNull();

    List<CourseJpaEntity> findByStatusAndDeletedAtIsNull(CourseStatus status);

    List<CourseJpaEntity> findByCourseCategory_CourseCategoryIdAndStatusAndDeletedAtIsNull(
            Long courseCategoryId,
            CourseStatus status
    );

    List<CourseJpaEntity> findByCourseCategory_CourseCategoryIdAndDeletedAtIsNull(Long courseCategoryId);

    Optional<CourseJpaEntity> findByCourseIdAndDeletedAtIsNull(Long courseId);

    Optional<CourseJpaEntity> findByCourseIdAndStatusAndDeletedAtIsNull(Long courseId, CourseStatus status);

    List<CourseJpaEntity> findByInstructorIdAndDeletedAtIsNull(Long instructorId);

    @Transactional
    default int hardDeleteByDeletedAtBefore(LocalDateTime threshold) {
        List<Long> courseIds = findHardDeleteTargetIds(threshold);
        if (courseIds.isEmpty()) {
            return 0;
        }

        List<Long> lectureIds = findLectureIdsByCourseIds(courseIds);
        List<Long> lectureProblemSetIds = findLectureProblemSetIdsByCourseIds(courseIds);

        if (!lectureProblemSetIds.isEmpty()) {
            deleteLectureProblemProgressesByLectureProblemSetIds(lectureProblemSetIds);
        }
        if (!lectureIds.isEmpty()) {
            deleteLectureProgressesByLectureIds(lectureIds);
        }

        deleteLectureProblemSetsByCourseIds(courseIds);
        deleteLecturesByCourseIds(courseIds);
        deleteEnrollmentsByCourseIds(courseIds);
        return deleteCoursesByIds(courseIds);
    }

    @Query("""
            select c.courseId
            from CourseJpaEntity c
            where c.deletedAt is not null
              and c.deletedAt < :threshold
            """)
    List<Long> findHardDeleteTargetIds(@Param("threshold") LocalDateTime threshold);

    @Query("""
            select l.lectureId
            from LectureJpaEntity l
            where l.courseId in :courseIds
            """)
    List<Long> findLectureIdsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("""
            select cps.courseProblemSetId
            from CourseProblemSetJpaEntity cps
            where cps.course.courseId in :courseIds
            """)
    List<Long> findLectureProblemSetIdsByCourseIds(@Param("courseIds") List<Long> courseIds);

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
            delete from LectureProgressJpaEntity p
            where p.lectureId in :lectureIds
            """)
    int deleteLectureProgressesByLectureIds(@Param("lectureIds") List<Long> lectureIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from CourseProblemSetJpaEntity cps
            where cps.course.courseId in :courseIds
            """)
    int deleteLectureProblemSetsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from LectureJpaEntity l
            where l.courseId in :courseIds
            """)
    int deleteLecturesByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from EnrollmentJpaEntity e
            where e.courseId in :courseIds
            """)
    int deleteEnrollmentsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from CourseJpaEntity c
            where c.courseId in :courseIds
            """)
    int deleteCoursesByIds(@Param("courseIds") List<Long> courseIds);
}
