package com.wanted.codebombalms.problems.result.presentation;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.result.application.query.GetProblemSetResultQuery;
import com.wanted.codebombalms.problems.result.application.usecase.GetProblemSetResultUseCase;
import com.wanted.codebombalms.problems.result.presentation.response.ProblemSetResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "문제 세트 결과", description = "학생의 문제 세트 풀이 완료 여부, 정답률, 문제별 제출 결과 조회 API")
@RestController
@RequiredArgsConstructor
public class ResultController {

    private final GetProblemSetResultUseCase getProblemSetResultUseCase;

    @Operation(
            summary = "문제 세트 결과 조회",
            description = "특정 학생의 문제 세트 풀이 결과를 조회합니다. "
                    + "응답에는 문제 세트 완료 여부, 정답률, 전체 완료자 수, 정답 완료자 수, 문제별 제출 답안과 해설이 포함됩니다. "
                    + "문제 세트를 끝까지 풀지 않은 경우 결과를 조회할 수 없습니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "문제 세트 결과 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 세트 결과 조회 성공",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 200,
                                          "code": "COMMON-SUCCESS",
                                          "message": "요청이 성공적으로 처리되었습니다.",
                                          "data": {
                                            "problemSetId": 3001,
                                            "title": "pandas 기초 분석 문제 세트",
                                            "isCompleted": true,
                                            "accuracyRate": 75.0,
                                            "totalCompletedUserCount": 12,
                                            "correctCompletedUserCount": 9,
                                            "submissions": [
                                              {
                                                "problemId": 3001,
                                                "problemNumber": 1,
                                                "title": "데이터 행과 열 개수 확인",
                                                "content": "employee_performance.csv 파일을 불러온 뒤 DataFrame의 행과 열 개수를 확인하세요.",
                                                "submittedCode": "result = df.shape",
                                                "isCorrect": true,
                                                "submittedAt": "2026-05-27T11:30:00",
                                                "explanation": "df.shape는 (행 개수, 열 개수) 튜플을 반환합니다."
                                              },
                                              {
                                                "problemId": 3002,
                                                "problemNumber": 2,
                                                "title": "평균 점수 계산",
                                                "content": "score 컬럼의 평균값을 구하세요.",
                                                "submittedCode": "result = df['score'].mean()",
                                                "isCorrect": false,
                                                "submittedAt": "2026-05-27T11:40:00",
                                                "explanation": "mean() 함수를 사용하면 숫자 컬럼의 평균을 계산할 수 있습니다."
                                              }
                                            ]
                                          }
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-SET-001 - 문제 세트를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 세트 없음",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 404,
                                          "code": "PRB-SET-001",
                                          "message": "문제 세트를 찾을 수 없습니다.",
                                          "path": "/api/v1/problem-sets/9999/result"
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "PRB-SET-004 - 문제 세트를 끝까지 풀지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 세트 미완료",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 409,
                                          "code": "PRB-SET-004",
                                          "message": "문제 세트를 끝까지 풀지 않았습니다.",
                                          "path": "/api/v1/problem-sets/3001/result"
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/api/v1/problem-sets/{problemSetId}/result")
    public ResponseEntity<ApiResponse<ProblemSetResultResponse>> findResult(
            @Parameter(description = "결과를 조회할 문제 세트 ID", example = "3001")
            @PathVariable Long problemSetId,

            @AuthenticationPrincipal Long userId
    ) {
        if (userId == null) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_REQUIRED);
        }

        var query = new GetProblemSetResultQuery(problemSetId, userId);

        var response = new ProblemSetResultResponse(
                getProblemSetResultUseCase.handle(query)
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }
}
