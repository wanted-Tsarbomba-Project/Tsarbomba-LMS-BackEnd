package com.wanted.codebombalms.problems.result.presentation.api;

import com.wanted.codebombalms.problems.result.application.query.GetProblemSetResultQuery;
import com.wanted.codebombalms.problems.result.application.usecase.GetProblemSetResultUseCase;
import com.wanted.codebombalms.problems.result.presentation.api.response.ProblemSetResultResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ResultController {

    private final GetProblemSetResultUseCase getProblemSetResultUseCase;

    @GetMapping("/api/v1/problem-sets/{problemSetId}/result")
    public ResponseEntity<ApiResponse<ProblemSetResultResponse>> findResult(
            @PathVariable Long problemSetId,
            @RequestParam Long userId
    ) {
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
