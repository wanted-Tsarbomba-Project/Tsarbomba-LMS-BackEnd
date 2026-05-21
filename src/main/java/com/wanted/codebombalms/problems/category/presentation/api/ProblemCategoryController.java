package com.wanted.codebombalms.problems.category.presentation.api;

import com.wanted.codebombalms.problems.category.application.usecase.GetProblemCategoriesUseCase;
import com.wanted.codebombalms.problems.category.presentation.api.response.ProblemCategoryResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
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
    public ResponseEntity<ApiResponse<List<ProblemCategoryResponse>>> findCategories() {
        List<ProblemCategoryResponse> response = getProblemCategoriesUseCase.handle()
                .stream()
                .map(ProblemCategoryResponse::new)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }
}
