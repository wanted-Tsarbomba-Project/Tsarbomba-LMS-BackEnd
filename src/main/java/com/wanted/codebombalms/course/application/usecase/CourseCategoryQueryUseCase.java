package com.wanted.codebombalms.course.application.usecase;

import com.wanted.codebombalms.course.domain.model.CourseCategory;

import java.util.List;

public interface CourseCategoryQueryUseCase {

    List<CourseCategory> findCourseCategories();
}
