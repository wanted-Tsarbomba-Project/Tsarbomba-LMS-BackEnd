package com.wanted.codebombalms.course.domain.repository;

import com.wanted.codebombalms.course.domain.model.CourseCategory;

import java.util.List;
import java.util.Optional;

public interface CourseCategoryRepository {

    List<CourseCategory> findActiveCategories();

    Optional<CourseCategory> findActiveCategoryById(Long courseCategoryId);

    boolean existsActiveCategory(Long courseCategoryId);
}
