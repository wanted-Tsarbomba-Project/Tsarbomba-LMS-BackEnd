package com.wanted.codebombalms.domain.problems.category.controller;

import com.wanted.codebombalms.domain.problems.category.application.usecase.GetProblemCategoriesUseCase;
import com.wanted.codebombalms.domain.problems.category.presentation.response.ProblemCategoryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/problem-categories")
public class ProblemCategoryController {

    private final GetProblemCategoriesUseCase getProblemCategoriesUseCase;

    public ProblemCategoryController(GetProblemCategoriesUseCase getProblemCategoriesUseCase) {
        this.getProblemCategoriesUseCase = getProblemCategoriesUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ProblemCategoryResponse>> findCategories() {
        return ResponseEntity.ok(
                getProblemCategoriesUseCase.getActiveCategories()
                        .stream()
                        .map(ProblemCategoryResponse::new)
                        .toList()
        );
    }
}
