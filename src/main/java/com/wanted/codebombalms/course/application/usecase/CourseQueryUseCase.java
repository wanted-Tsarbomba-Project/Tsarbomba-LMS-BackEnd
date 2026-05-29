package com.wanted.codebombalms.course.application.usecase;

import com.wanted.codebombalms.course.domain.model.Course;

import java.util.List;

public interface CourseQueryUseCase {

    List<Course> findAllCourses(Long courseCategoryId);

    List<Course> findAllCoursesForOperator(Long courseCategoryId);

    List<Course> findCoursesByInstructor(Long instructorId);

    Course findCourseById(Long courseId);

    Course findCourseByIdForOperator(Long courseId);
}
