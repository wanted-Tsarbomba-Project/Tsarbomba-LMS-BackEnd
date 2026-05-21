package com.wanted.codebombalms.problems.set.presentation.api;

import com.wanted.codebombalms.problems.set.application.query.GetProblemSetsQuery;
import com.wanted.codebombalms.problems.set.application.query.EnterProblemSetQuery;
import com.wanted.codebombalms.problems.set.application.usecase.EnterProblemSetUseCase;
import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetsUseCase;
import com.wanted.codebombalms.problems.set.presentation.api.response.ProblemSetEnterResponse;
import com.wanted.codebombalms.problems.set.presentation.api.response.ProblemSetListResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProblemSetController {

    private final GetProblemSetsUseCase getProblemSetsUseCase;
    private final EnterProblemSetUseCase enterProblemSetUseCase;

    @GetMapping("/api/v1/problem-sets")
    public ResponseEntity<ApiResponse<List<ProblemSetListResponse>>> findProblemSets(
            @RequestParam Long categoryId
    ) {
        var query = new GetProblemSetsQuery(categoryId);
        var response = getProblemSetsUseCase.handle(query)
                .stream()
                .map(ProblemSetListResponse::new)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }

    @GetMapping("/api/v1/problem-sets/{problemSetId}")
    public ResponseEntity<ApiResponse<ProblemSetEnterResponse>> enterProblemSet(
            @PathVariable Long problemSetId,
            @RequestParam Long userId
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
