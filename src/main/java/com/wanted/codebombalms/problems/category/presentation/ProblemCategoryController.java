package com.wanted.codebombalms.problems.category.presentation;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.category.application.usecase.GetProblemCategoriesUseCase;
import com.wanted.codebombalms.problems.category.presentation.response.ProblemCategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "문제 카테고리", description = "문제 등록과 문제 세트 목록 필터에 사용할 카테고리 조회 API")
@RestController
@RequestMapping("/api/v1/problem-categories")
public class ProblemCategoryController {

    private final GetProblemCategoriesUseCase getProblemCategoriesUseCase;

    public ProblemCategoryController(GetProblemCategoriesUseCase getProblemCategoriesUseCase) {
        this.getProblemCategoriesUseCase = getProblemCategoriesUseCase;
    }

    @Operation(
            summary = "문제 카테고리 목록 조회",
            description = "활성화된 문제 카테고리 목록을 조회합니다. "
                    + "프론트에서 문제 등록 화면의 카테고리 선택값이나 문제 세트 목록 필터로 사용할 수 있습니다. "
                    + "카테고리 ID는 문제 세트 목록 조회의 categoryId query parameter로 사용합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "문제 카테고리 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "카테고리 목록 조회 성공",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 200,
                                              "code": "COMMON-SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": [
                                                {
                                                  "categoryId": 3001,
                                                  "categoryName": "Python 데이터 분석",
                                                  "description": "Python과 pandas를 활용한 코드 실행형 문제 분야입니다."
                                                },
                                                {
                                                  "categoryId": 3002,
                                                  "categoryName": "SQL 기초",
                                                  "description": "SELECT, WHERE, GROUP BY를 연습하는 문제 분야입니다."
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "PRB-002 - 문제 도메인 서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "서버 오류",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 500,
                                              "code": "PRB-002",
                                              "message": "문제 도메인 처리 중 서버 오류가 발생했습니다.",
                                              "path": "/api/v1/problem-categories"
                                            }
                                            """
                            )
                    )
            )
    })
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
