package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.CourseCategory;
import com.wanted.codebombalms.course.domain.model.CourseCategoryStatus;
import com.wanted.codebombalms.course.domain.repository.CourseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CourseCategoryRepositoryAdapter implements CourseCategoryRepository {

    private final SpringDataCourseCategoryRepository springDataCourseCategoryRepository;

    @Override
    public List<CourseCategory> findActiveCategories() {
        return springDataCourseCategoryRepository.findByStatusOrderByDisplayOrderAsc(CourseCategoryStatus.ACTIVE)
                .stream()
                .map(CourseCategoryJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<CourseCategory> findActiveCategoryById(Long courseCategoryId) {
        return springDataCourseCategoryRepository.findByCourseCategoryIdAndStatus(
                        courseCategoryId,
                        CourseCategoryStatus.ACTIVE
                )
                .map(CourseCategoryJpaEntity::toDomain);
    }

    @Override
    public boolean existsActiveCategory(Long courseCategoryId) {
        return springDataCourseCategoryRepository.existsByCourseCategoryIdAndStatus(
                courseCategoryId,
                CourseCategoryStatus.ACTIVE
        );
    }
}
