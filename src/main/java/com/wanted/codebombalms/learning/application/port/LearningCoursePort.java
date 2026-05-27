package com.wanted.codebombalms.learning.application.port;

import com.wanted.codebombalms.learning.domain.model.LearningCourse;
import java.util.List;

public interface LearningCoursePort {

    List<LearningCourse> findActiveCourses();

    LearningCourse findActiveCourse(Long courseId);
}
