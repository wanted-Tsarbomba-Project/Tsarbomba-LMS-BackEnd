package com.wanted.codebombalms.domain.course.domain.repository;

import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.course.domain.model.CourseStatus;

import java.util.List;
import java.util.Optional;

public interface CourseRepository {

    Course save(Course course);

    List<Course> findByDeletedAtIsNull();

    List<Course> findByStatusAndDeletedAtIsNull(CourseStatus status);

    Optional<Course> findByCourseIdAndDeletedAtIsNull(Long courseId);

    Optional<Course> findByCourseIdAndStatusAndDeletedAtIsNull(Long courseId, CourseStatus status);

    List<Course> findByInstructorIdAndDeletedAtIsNull(Long instructorId);
}
