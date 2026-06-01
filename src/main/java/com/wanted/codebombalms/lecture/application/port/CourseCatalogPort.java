package com.wanted.codebombalms.lecture.application.port;

import com.wanted.codebombalms.course.domain.model.Course;

public interface CourseCatalogPort {

    Course findCourse(Long courseId);
}
