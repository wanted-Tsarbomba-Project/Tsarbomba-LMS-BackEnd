package com.wanted.codebombalms.course.application.usecase;

import com.wanted.codebombalms.course.domain.model.Course;

import java.util.List;

public interface CourseQueryUseCase {

    List<Course> findAllCourses();

    Course findCourseById(Long courseId);
}
