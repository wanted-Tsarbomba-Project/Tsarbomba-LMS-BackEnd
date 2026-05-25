package com.wanted.codebombalms.course.application.policy;

import com.wanted.codebombalms.course.domain.exception.CourseErrorCode;
import com.wanted.codebombalms.course.domain.repository.CourseCategoryRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseCategoryPolicy {

    private final CourseCategoryRepository courseCategoryRepository;

    public void validateActiveCategory(Long courseCategoryId) {
        if (courseCategoryId == null || !courseCategoryRepository.existsActiveCategory(courseCategoryId)) {
            throw new ValidationException(CourseErrorCode.COURSE_CATEGORY_REQUIRED);
        }
    }
}
