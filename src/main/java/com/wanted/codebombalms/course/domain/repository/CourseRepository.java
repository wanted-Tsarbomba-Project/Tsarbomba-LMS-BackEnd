package com.wanted.codebombalms.course.domain.repository;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CourseRepository {

    Course save(Course course);

    List<Course> findByDeletedAtIsNull();

    List<Course> findByStatusAndDeletedAtIsNull(CourseStatus status);

    List<Course> findByCourseCategoryIdAndStatusAndDeletedAtIsNull(Long courseCategoryId, CourseStatus status);

    List<Course> findByCourseCategoryIdAndDeletedAtIsNull(Long courseCategoryId);

    Optional<Course> findByCourseIdAndDeletedAtIsNull(Long courseId);

    List<Course> findByCourseIdInAndDeletedAtIsNull(Set<Long> courseIds);

    Optional<Course> findByCourseIdAndStatusAndDeletedAtIsNull(Long courseId, CourseStatus status);

    List<Course> findByInstructorIdAndDeletedAtIsNull(Long instructorId);
}
