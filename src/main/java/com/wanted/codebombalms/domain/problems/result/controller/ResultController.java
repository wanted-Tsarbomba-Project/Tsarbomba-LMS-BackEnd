package com.wanted.codebombalms.domain.problems.result.controller;

import com.wanted.codebombalms.domain.problems.result.dto.response.ProblemSetResultResponse;
import com.wanted.codebombalms.domain.problems.result.service.ResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/api/v1/problem-sets/{problemSetId}/result")
    public ResponseEntity<ProblemSetResultResponse> findResult(
            @PathVariable Long problemSetId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(resultService.findResult(problemSetId, userId));
    }
}
