package com.wanted.codebombalms.domain.problems.set.controller;

import com.wanted.codebombalms.domain.problems.set.dto.response.ProblemSetEnterResponse;
import com.wanted.codebombalms.domain.problems.set.dto.response.ProblemSetListResponse;
import com.wanted.codebombalms.domain.problems.set.service.ProblemSetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProblemSetController {

    private final ProblemSetService problemSetService;

    public ProblemSetController(ProblemSetService problemSetService) {
        this.problemSetService = problemSetService;
    }

    @GetMapping("/api/v1/problem-sets")
    public ResponseEntity<List<ProblemSetListResponse>> findProblemSets(
            @RequestParam Long categoryId
    ) {
        return ResponseEntity.ok(problemSetService.findActiveSetsByCategory(categoryId));
    }
    @GetMapping("/api/v1/problem-sets/{problemSetId}")
    public ResponseEntity<ProblemSetEnterResponse> enterProblemSet(
            @PathVariable Long problemSetId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(problemSetService.enterProblemSet(problemSetId, userId));
    }
}
