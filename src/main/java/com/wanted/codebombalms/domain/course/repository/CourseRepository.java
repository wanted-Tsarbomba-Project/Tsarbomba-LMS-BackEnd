package com.wanted.codebombalms.domain.course.repository;

import com.wanted.codebombalms.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByDeletedAtIsNull();

    Optional<Course> findByCourseIdAndDeletedAtIsNull(Long courseId);

    List<Course> findByInstructorIdAndDeletedAtIsNull(Long instructorId);
}