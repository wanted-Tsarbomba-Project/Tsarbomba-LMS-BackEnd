package com.wanted.codebombalms.lecture.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataLectureRepository extends JpaRepository<LectureJpaEntity, Long> {

    List<LectureJpaEntity> findByDeletedAtIsNull();

    Optional<LectureJpaEntity> findByLectureIdAndDeletedAtIsNull(Long lectureId);

    List<LectureJpaEntity> findByCourse_CourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(Long courseId);

    List<LectureJpaEntity> findByCourse_CourseIdAndDeletedAtIsNull(Long courseId);

    boolean existsByCourse_CourseIdAndDeletedAtIsNull(Long courseId);

    boolean existsByCourse_CourseIdAndLectureIdAndDeletedAtIsNull(Long courseId, Long lectureId);
}
