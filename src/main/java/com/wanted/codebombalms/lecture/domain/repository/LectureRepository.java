package com.wanted.codebombalms.lecture.domain.repository;

import com.wanted.codebombalms.lecture.domain.model.Lecture;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LectureRepository {

    Lecture save(Lecture lecture);

    List<Lecture> findByDeletedAtIsNull();

    Optional<Lecture> findByLectureIdAndDeletedAtIsNull(Long lectureId);

    List<Lecture> findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(Long courseId);

    Map<Long, Long> countActiveByCourseIds(Collection<Long> courseIds);

    List<Long> findPreviousLectureIds(Long courseId, Integer lectureOrder);

    boolean existsNextLecture(Long courseId, Integer lectureOrder);

    boolean existsByCourseIdAndDeletedAtIsNull(Long courseId);

    boolean existsByCourseIdAndLectureIdAndDeletedAtIsNull(Long courseId, Long lectureId);
}
