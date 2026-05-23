package com.wanted.codebombalms.course.presentation.api.response;

import com.wanted.codebombalms.course.domain.model.CourseCategory;

public record CourseCategoryResponse(
        Long courseCategoryId,
        String name,
        Integer displayOrder
) {

    public static CourseCategoryResponse from(CourseCategory courseCategory) {
        return new CourseCategoryResponse(
                courseCategory.getCourseCategoryId(),
                courseCategory.getName(),
                courseCategory.getDisplayOrder()
        );
    }
}
