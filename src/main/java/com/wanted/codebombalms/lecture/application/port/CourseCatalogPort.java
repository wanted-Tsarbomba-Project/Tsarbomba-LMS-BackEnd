package com.wanted.codebombalms.lecture.application.port;

import com.wanted.codebombalms.course.domain.model.Course;

import java.util.Map;
import java.util.Set;

public interface CourseCatalogPort {

    Course findCourse(Long courseId);

    Map<Long, Course> findCourses(Set<Long> courseIds);
}
