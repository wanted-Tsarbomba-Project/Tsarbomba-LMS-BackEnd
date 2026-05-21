package com.wanted.codebombalms.domain.course.application.usecase;

import com.wanted.codebombalms.domain.course.application.result.CourseDetailResult;
import com.wanted.codebombalms.domain.course.application.result.CourseSummaryResult;

import java.util.List;

public interface CourseQueryUseCase {

    List<CourseSummaryResult> findAllCourses();

    CourseDetailResult findCourseById(Long courseId);
}
