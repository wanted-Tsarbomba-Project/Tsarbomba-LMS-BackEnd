package com.wanted.codebombalms.problems.hint.presentation;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.hint.application.usecase.FindProblemHintsUseCase;
import com.wanted.codebombalms.problems.hint.presentation.response.ProblemHintResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "문제 힌트", description = "학생이 문제 풀이 중 확인할 수 있는 힌트 조회 API")
@RestController
public class ProblemHintController {

    private final FindProblemHintsUseCase findProblemHintsUseCase;

    public ProblemHintController(FindProblemHintsUseCase findProblemHintsUseCase) {
        this.findProblemHintsUseCase = findProblemHintsUseCase;
    }

    @Operation(
            summary = "문제 힌트 목록 조회",
            description = "특정 문제의 힌트 목록을 순서대로 조회합니다. "
                    + "힌트는 정답을 직접 노출하는 값이 아니라 문제 풀이 방향을 안내하는 도움말입니다. "
                    + "프론트는 hintOrder 순서대로 단계적으로 힌트를 노출할 수 있습니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "문제 힌트 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "힌트 목록 조회 성공",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 200,
                                              "code": "COMMON-SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": [
                                                {
                                                  "hintId": 3001,
                                                  "hintOrder": 1,
                                                  "hintContent": "DataFrame의 shape 속성을 사용해보세요."
                                                },
                                                {
                                                  "hintId": 3002,
                                                  "hintOrder": 2,
                                                  "hintContent": "shape 결과는 (행 개수, 열 개수) 형태의 튜플입니다."
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-PBL-001 - 문제를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 없음",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 404,
                                              "code": "PRB-PBL-001",
                                              "message": "문제를 찾을 수 없습니다.",
                                              "path": "/api/v1/problems/9999/hints"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/api/v1/problems/{problemId}/hints")
    public ResponseEntity<ApiResponse<List<ProblemHintResponse>>> findHints(
            @Parameter(description = "힌트를 조회할 문제 ID", example = "3001")
            @PathVariable Long problemId
    ) {
        List<ProblemHintResponse> response = findProblemHintsUseCase.handle(problemId)
                .stream()
                .map(ProblemHintResponse::new)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }
}
