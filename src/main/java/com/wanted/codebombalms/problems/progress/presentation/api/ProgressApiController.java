package com.wanted.codebombalms.problems.progress.presentation.api;

import com.wanted.codebombalms.problems.progress.application.query.GetProblemProgressQuery;
import com.wanted.codebombalms.problems.progress.application.usecase.GetProblemProgressUseCase;
import com.wanted.codebombalms.problems.progress.presentation.api.response.ProblemProgressResponse;
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
public class ProgressApiController {

    private final GetProblemProgressUseCase getProblemProgressUseCase;

    @GetMapping("/api/v1/problem-sets/{problemSetId}/progress")
    public ResponseEntity<ApiResponse<ProblemProgressResponse>> findProblemSetProgress(
            @PathVariable Long problemSetId,
            @RequestParam Long userId
    ) {
        var query = new GetProblemProgressQuery(problemSetId, userId);

        var response = new ProblemProgressResponse(
                getProblemProgressUseCase.handle(query)
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }
}
