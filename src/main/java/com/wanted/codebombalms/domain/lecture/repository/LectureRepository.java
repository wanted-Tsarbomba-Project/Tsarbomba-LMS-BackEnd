package com.wanted.codebombalms.domain.lecture.repository;

import com.wanted.codebombalms.domain.lecture.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {

    List<Lecture> findByDeletedAtIsNull();

    Optional<Lecture> findByLectureIdAndDeletedAtIsNull(Long lectureId);

    List<Lecture> findByCourse_CourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(Long courseId);

}