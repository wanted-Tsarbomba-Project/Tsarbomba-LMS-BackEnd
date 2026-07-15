package com.wanted.codebombalms.problems.category.presentation;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.category.application.usecase.ManageProblemCategoriesUseCase;
import com.wanted.codebombalms.problems.category.presentation.request.ProblemCategoryCreateRequest;
import com.wanted.codebombalms.problems.category.presentation.request.ProblemCategoryUpdateRequest;
import com.wanted.codebombalms.problems.category.presentation.response.ProblemCategoryAdminResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/problem-categories")
@RequiredArgsConstructor
@Tag(name = "문제 카테고리 관리", description = "관리자/운영자의 문제 카테고리 생성, 조회, 수정, 비활성화, 활성화 API")
public class ProblemCategoryAdminController {

    private final ManageProblemCategoriesUseCase manageProblemCategoriesUseCase;

    @Operation(
            summary = "관리자 문제 카테고리 목록 조회",
            description = "ACTIVE와 INACTIVE 상태의 문제 카테고리를 모두 조회합니다. 카테고리 관리 페이지에서 사용합니다."
    )
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

    @Operation(
            summary = "문제 카테고리 생성",
            description = "새 문제 카테고리를 ACTIVE 상태로 생성합니다. ACTIVE 카테고리 이름과 중복되면 실패합니다."
    )
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

    @Operation(
            summary = "문제 카테고리 이름 수정",
            description = "지정한 문제 카테고리의 이름을 수정합니다. ACTIVE 카테고리 이름과 중복되면 실패합니다."
    )
    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<ProblemCategoryAdminResponse>> updateName(
            @Parameter(description = "수정할 문제 카테고리 ID", example = "1")
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

    @Operation(
            summary = "문제 카테고리 비활성화",
            description = "지정한 문제 카테고리를 INACTIVE 상태로 변경합니다. 해당 카테고리를 참조하는 ACTIVE 문제세트가 있으면 실패합니다."
    )
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<ProblemCategoryAdminResponse>> deactivate(
            @Parameter(description = "비활성화할 문제 카테고리 ID", example = "1")
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

    @Operation(
            summary = "문제 카테고리 활성화",
            description = "INACTIVE 상태의 문제 카테고리를 ACTIVE 상태로 복구합니다. 같은 이름의 ACTIVE 카테고리가 이미 있으면 실패합니다."
    )
    @PatchMapping("/{categoryId}/activate")
    public ResponseEntity<ApiResponse<ProblemCategoryAdminResponse>> activate(
            @Parameter(description = "활성화할 문제 카테고리 ID", example = "1")
            @PathVariable Long categoryId
    ) {
        ProblemCategoryAdminResponse response = new ProblemCategoryAdminResponse(
                manageProblemCategoriesUseCase.activate(categoryId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }
}
