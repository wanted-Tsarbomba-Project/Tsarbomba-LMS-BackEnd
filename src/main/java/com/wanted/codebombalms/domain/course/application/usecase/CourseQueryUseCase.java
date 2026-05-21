package com.wanted.codebombalms.domain.course.application.usecase;

import com.wanted.codebombalms.domain.course.presentation.api.response.CourseDetailResponse;
import com.wanted.codebombalms.domain.course.presentation.api.response.CourseResponse;

import java.util.List;

public interface CourseQueryUseCase {

    List<CourseResponse> findAllCourses();

    CourseDetailResponse findCourseById(Long courseId);
}
