package com.wanted.codebombalms.problems.set.presentation;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.set.application.query.EnterProblemSetQuery;
import com.wanted.codebombalms.problems.set.application.query.GetProblemSetsQuery;
import com.wanted.codebombalms.problems.set.application.usecase.EnterProblemSetUseCase;
import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetsUseCase;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetEnterResponse;
import com.wanted.codebombalms.problems.set.presentation.response.ProblemSetPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "문제 세트", description = "학생이 문제 세트 목록을 조회하고 문제 풀이에 진입하는 API")
@RestController
@RequiredArgsConstructor
public class ProblemSetController {

    private final GetProblemSetsUseCase getProblemSetsUseCase;
    private final EnterProblemSetUseCase enterProblemSetUseCase;

    @Operation(
            summary = "문제 세트 목록 조회",
            description = "카테고리 ID를 기준으로 활성 문제 세트 목록을 조회합니다. "
                    + "응답에는 문제 세트 ID, 제목, 설명, 난이도, 정답률, 생성일이 포함됩니다. "
                    + "카테고리별 문제 목록 화면에서 사용합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "문제 세트 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 세트 목록",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 200,
                                              "code": "COMMON-SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": [
                                                {
                                                  "problemSetId": 3001,
                                                  "problemNumber": 1,
                                                  "title": "pandas 기초 분석 문제 세트",
                                                  "description": "CSV 데이터를 불러와 기본 정보를 확인하는 문제 세트입니다.",
                                                  "difficulty": "EASY",
                                                  "accuracyRate": 75.5,
                                                  "createdAt": "2026-05-27T10:00:00"
                                                },
                                                {
                                                  "problemSetId": 3002,
                                                  "problemNumber": 2,
                                                  "title": "DataFrame 필터링 문제 세트",
                                                  "description": "조건에 맞는 데이터를 필터링하는 문제 세트입니다.",
                                                  "difficulty": "MEDIUM",
                                                  "accuracyRate": null,
                                                  "createdAt": "2026-05-27T11:00:00"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PRB-CAT-002 - 잘못된 문제 분야",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "잘못된 카테고리",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 400,
                                              "code": "PRB-CAT-002",
                                              "message": "잘못된 문제 분야입니다.",
                                              "path": "/api/v1/problem-sets"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-CAT-001 - 문제 분야를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "카테고리 없음",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 404,
                                              "code": "PRB-CAT-001",
                                              "message": "문제 분야를 찾을 수 없습니다.",
                                              "path": "/api/v1/problem-sets"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/api/v1/problem-sets")
    public ResponseEntity<ApiResponse<ProblemSetPageResponse>> findProblemSets(
            @Parameter(description = "조회할 문제 카테고리 ID", example = "3001")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        var query = new GetProblemSetsQuery(categoryId, page, size);
        var response = ProblemSetPageResponse.from(getProblemSetsUseCase.handle(query));

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }

    @Operation(
            summary = "문제 세트 진입",
            description = "학생이 문제 세트에 진입할 때 이어풀기 상태를 기준으로 현재 풀어야 할 문제를 반환합니다. "
                    + "처음 진입하는 경우 진행 상태를 생성하고 1번 문제를 반환할 수 있습니다. "
                    + "완료한 문제 세트는 isCompleted가 true로 반환됩니다. "
                    + "문제 화면에 표시할 현재 소문제와 startCode를 함께 제공합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "문제 세트 진입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "현재 풀 문제 반환",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 200,
                                                      "code": "COMMON-SUCCESS",
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "problemSetId": 3001,
                                                        "title": "pandas 기초 분석 문제 세트",
                                                        "description": "CSV 데이터를 불러와 기본 정보를 확인하는 문제 세트입니다.",
                                                        "currentProblemNumber": 1,
                                                        "isCompleted": false,
                                                        "problem": {
                                                          "problemId": 3001,
                                                          "problemNumber": 1,
                                                          "title": "데이터 행과 열 개수 확인",
                                                          "content": "employee_performance.csv 파일을 불러온 뒤 DataFrame의 행과 열 개수를 확인하세요.",
                                                          "problemType": "CODE",
                                                          "point": 10,
                                                          "startCode": "import os\\nimport pandas as pd\\n\\ndf = pd.read_csv(os.environ[\\"DATASET_PATH\\"])"
                                                        }
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "이미 완료한 문제 세트",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 200,
                                                      "code": "COMMON-SUCCESS",
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "problemSetId": 3001,
                                                        "title": "pandas 기초 분석 문제 세트",
                                                        "description": "CSV 데이터를 불러와 기본 정보를 확인하는 문제 세트입니다.",
                                                        "currentProblemNumber": null,
                                                        "isCompleted": true,
                                                        "problem": null
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "AUT-003 - 인증 토큰 만료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "인증 토큰 만료",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 401,
                                              "code": "AUT-003",
                                              "message": "만료된 토큰입니다.",
                                              "path": "/api/v1/problem-sets/3001"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-SET-001 - 문제 세트를 찾을 수 없음 또는 USR-001 - 사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "문제 세트 없음",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 404,
                                                      "code": "PRB-SET-001",
                                                      "message": "문제 세트를 찾을 수 없습니다.",
                                                      "path": "/api/v1/problem-sets/9999"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "사용자 없음",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 404,
                                                      "code": "USR-001",
                                                      "message": "존재하지 않는 회원입니다.",
                                                      "path": "/api/v1/problem-sets/3001"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/v1/problem-sets/{problemSetId}")
    public ResponseEntity<ApiResponse<ProblemSetEnterResponse>> enterProblemSet(
            @PathVariable Long problemSetId,
            @AuthenticationPrincipal Long userId
    ) {
        var query = new EnterProblemSetQuery(problemSetId, userId);

        var response = new ProblemSetEnterResponse(
                enterProblemSetUseCase.handle(query)
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }
}
