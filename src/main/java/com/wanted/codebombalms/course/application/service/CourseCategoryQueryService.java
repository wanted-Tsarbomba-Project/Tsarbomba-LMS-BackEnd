package com.wanted.codebombalms.course.application.service;

import com.wanted.codebombalms.course.application.usecase.CourseCategoryQueryUseCase;
import com.wanted.codebombalms.course.domain.model.CourseCategory;
import com.wanted.codebombalms.course.domain.repository.CourseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseCategoryQueryService implements CourseCategoryQueryUseCase {

    private final CourseCategoryRepository courseCategoryRepository;

    @Override
    public List<CourseCategory> findCourseCategories() {
        return courseCategoryRepository.findActiveCategories();
    }
}
