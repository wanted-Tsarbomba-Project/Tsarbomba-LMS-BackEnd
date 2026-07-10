package com.wanted.codebombalms.problems.category.presentation;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.category.application.usecase.ManageProblemCategoriesUseCase;
import com.wanted.codebombalms.problems.category.presentation.request.ProblemCategoryCreateRequest;
import com.wanted.codebombalms.problems.category.presentation.request.ProblemCategoryUpdateRequest;
import com.wanted.codebombalms.problems.category.presentation.response.ProblemCategoryAdminResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/problem-categories")
@RequiredArgsConstructor
public class ProblemCategoryAdminController {

    private final ManageProblemCategoriesUseCase manageProblemCategoriesUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProblemCategoryAdminResponse>>> findCategories() {
        List<ProblemCategoryAdminResponse> response = manageProblemCategoriesUseCase.findCategories()
                .stream()
                .map(ProblemCategoryAdminResponse::new)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProblemCategoryAdminResponse>> create(
            @RequestBody @Valid ProblemCategoryCreateRequest request
    ) {
        ProblemCategoryAdminResponse response = new ProblemCategoryAdminResponse(
                manageProblemCategoriesUseCase.create(request.categoryName())
        );

        return ResponseEntity.status(201).body(ApiResponse.created(
                ApiResponseCode.CREATED,
                ApiResponseMessage.CREATED,
                response
        ));
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<ProblemCategoryAdminResponse>> updateName(
            @PathVariable Long categoryId,
            @RequestBody @Valid ProblemCategoryUpdateRequest request
    ) {
        ProblemCategoryAdminResponse response = new ProblemCategoryAdminResponse(
                manageProblemCategoriesUseCase.updateName(categoryId, request.categoryName())
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<ProblemCategoryAdminResponse>> deactivate(
            @PathVariable Long categoryId
    ) {
        ProblemCategoryAdminResponse response = new ProblemCategoryAdminResponse(
                manageProblemCategoriesUseCase.deactivate(categoryId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }
}
