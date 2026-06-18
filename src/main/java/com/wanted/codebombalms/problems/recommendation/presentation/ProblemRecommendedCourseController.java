package com.wanted.codebombalms.problems.recommendation.presentation;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.recommendation.application.command.SaveProblemRecommendedCoursesCommand;
import com.wanted.codebombalms.problems.recommendation.application.query.GetProblemRecommendedCoursesQuery;
import com.wanted.codebombalms.problems.recommendation.application.query.GetRecommendedCourseEditViewQuery;
import com.wanted.codebombalms.problems.recommendation.application.query.GetSelectableRecommendedCoursesQuery;
import com.wanted.codebombalms.problems.recommendation.application.usecase.GetProblemRecommendedCoursesUseCase;
import com.wanted.codebombalms.problems.recommendation.application.usecase.GetRecommendedCourseEditViewUseCase;
import com.wanted.codebombalms.problems.recommendation.application.usecase.GetSelectableRecommendedCoursesUseCase;
import com.wanted.codebombalms.problems.recommendation.application.usecase.SaveProblemRecommendedCoursesUseCase;
import com.wanted.codebombalms.problems.recommendation.presentation.api.request.SaveProblemRecommendedCoursesRequest;
import com.wanted.codebombalms.problems.recommendation.presentation.api.response.RecommendedCourseEditViewResponse;
import com.wanted.codebombalms.problems.recommendation.presentation.api.response.RecommendedCourseResponse;
import com.wanted.codebombalms.problems.recommendation.presentation.api.response.SaveProblemRecommendedCoursesResponse;
import com.wanted.codebombalms.problems.recommendation.presentation.api.response.SelectableRecommendedCourseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProblemRecommendedCourseController {

    private final SaveProblemRecommendedCoursesUseCase saveProblemRecommendedCoursesUseCase;
    private final GetRecommendedCourseEditViewUseCase getRecommendedCourseEditViewUseCase;
    private final GetProblemRecommendedCoursesUseCase getProblemRecommendedCoursesUseCase;
    private final GetSelectableRecommendedCoursesUseCase getSelectableRecommendedCoursesUseCase;

    @Operation(
            summary = "문제 추천 코스 연결 저장",
            description = "운영자가 특정 문제에 추천 코스 목록을 연결합니다. 요청한 코스 ID 순서가 추천 노출 순서가 됩니다."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SaveProblemRecommendedCoursesRequest.class),
                    examples = @ExampleObject(
                            name = "추천 코스 연결 저장 요청",
                            value = """
                                    {
                                      "courseIds": [10, 11]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "추천 코스 연결 저장 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SaveProblemRecommendedCoursesResponse.class),
                            examples = @ExampleObject(
                                    name = "추천 코스 연결 저장 성공",
                                    value = """
                                            {
                                              "timestamp": "2026-06-17T12:10:00",
                                              "status": 200,
                                              "code": "COMMON-SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "problemId": 5164,
                                                "connectedCourseCount": 2,
                                                "courseIds": [10, 11]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "추천 코스 입력값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "추천 코스 연결 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문제 또는 코스를 찾을 수 없음")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PutMapping("/api/v1/problems/{problemId}/recommended-courses")
    public ResponseEntity<ApiResponse<SaveProblemRecommendedCoursesResponse>> saveRecommendedCourses(
            @Parameter(description = "추천 코스를 연결할 문제 ID", example = "5164")
            @PathVariable Long problemId,
            @RequestBody @Valid SaveProblemRecommendedCoursesRequest request
    ) {
        var result = saveProblemRecommendedCoursesUseCase.handle(
                new SaveProblemRecommendedCoursesCommand(
                        problemId,
                        request.courseIds()
                )
        );

        var response = SaveProblemRecommendedCoursesResponse.from(result);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }

    @Operation(
            summary = "문제 추천 코스 수정 화면 조회",
            description = "추천 코스 수정 화면에서 사용할 선택 가능 코스 목록과 현재 연결된 코스 상태를 함께 조회합니다."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @GetMapping("/api/v1/problems/{problemId}/recommended-courses/edit")
    public ResponseEntity<ApiResponse<RecommendedCourseEditViewResponse>> getRecommendedCourseEditView(
            @Parameter(description = "추천 코스 연결 상태를 조회할 문제 ID", example = "5164")
            @PathVariable Long problemId
    ) {
        var result = getRecommendedCourseEditViewUseCase.handle(
                new GetRecommendedCourseEditViewQuery(problemId)
        );

        var response = RecommendedCourseEditViewResponse.from(result);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }

    @Operation(
            summary = "학생용 문제 추천 코스 조회",
            description = "문제 풀이 화면에서 해당 문제와 연결된 추천 코스 목록을 조회합니다."
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/v1/problems/{problemId}/recommended-courses")
    public ResponseEntity<ApiResponse<RecommendedCourseResponse>> getRecommendedCourses(
            @Parameter(description = "추천 코스를 조회할 문제 ID", example = "5164")
            @PathVariable Long problemId
    ) {
        var result = getProblemRecommendedCoursesUseCase.handle(
                new GetProblemRecommendedCoursesQuery(problemId)
        );

        var response = RecommendedCourseResponse.from(result);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }

    @Operation(
            summary = "추천 코스 선택 목록 조회",
            description = "운영자가 문제에 추천 코스를 연결할 때 선택 가능한 ACTIVE 코스 목록을 조회합니다. 검색어와 최대 조회 개수를 지정할 수 있습니다."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @GetMapping("/api/v1/recommended-courses/selectable")
    public ResponseEntity<ApiResponse<SelectableRecommendedCourseResponse>> getSelectableRecommendedCourses(
            @Parameter(description = "코스 제목 검색어", example = "Pandas")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "최대 조회 개수. 기본 20, 최대 100", example = "20")
            @RequestParam(required = false) Integer limit
    ) {
        var result = getSelectableRecommendedCoursesUseCase.handle(
                new GetSelectableRecommendedCoursesQuery(keyword, limit)
        );

        var response = SelectableRecommendedCourseResponse.from(result);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }
}
