package com.wanted.codebombalms.domain.problems.progress.controller;

import com.wanted.codebombalms.domain.problems.progress.dto.response.ProblemProgressResponse;
import com.wanted.codebombalms.domain.problems.progress.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProgressApiController {

    private final ProgressService progressService;

    public ProgressApiController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/api/v1/problem-sets/{problemSetId}/progress")
    public ResponseEntity<ProblemProgressResponse> findProblemSetProgress(
            @PathVariable Long problemSetId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(progressService.findProblemSetProgress(problemSetId, userId));
    }
}
