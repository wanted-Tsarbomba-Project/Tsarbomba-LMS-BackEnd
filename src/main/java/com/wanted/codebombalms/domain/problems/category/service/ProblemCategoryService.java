package com.wanted.codebombalms.domain.problems.category.service;

import com.wanted.codebombalms.domain.problems.category.dto.response.CategoryResponse;
import com.wanted.codebombalms.domain.problems.category.entity.ProblemCategory;
import com.wanted.codebombalms.domain.problems.category.repository.ProblemCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProblemCategoryService {

    private final ProblemCategoryRepository problemCategoryRepository;

    public ProblemCategoryService(ProblemCategoryRepository problemCategoryRepository) {
        this.problemCategoryRepository = problemCategoryRepository;
    }

    public List<CategoryResponse> findActiveCategories() {
        return problemCategoryRepository.findByStatusOrderByCategoryIdAsc("ACTIVE")
                .stream()
                .map(CategoryResponse::new)
                .toList();
    }

    public boolean existsActiveCategory(Long categoryId) {
        return problemCategoryRepository.existsByCategoryIdAndStatus(categoryId, "ACTIVE");
    }

    public ProblemCategory findOrCreateActiveCategory(String categoryName) {
        String trimmedCategoryName = categoryName.trim();
        return problemCategoryRepository.findByCategoryNameAndStatus(
                trimmedCategoryName,"ACTIVE").orElseGet(() -> problemCategoryRepository.save(
                new ProblemCategory(
                        trimmedCategoryName,
                        trimmedCategoryName + " 문제 분야입니다."
                )
        ));
    }
}
