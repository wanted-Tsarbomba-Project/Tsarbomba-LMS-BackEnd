package com.wanted.codebombalms.domain.problems.hint.controller;

import com.wanted.codebombalms.domain.problems.hint.dto.response.ProblemHintResponse;
import com.wanted.codebombalms.domain.problems.hint.service.ProblemHintService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProblemHintController {

    private final ProblemHintService problemHintService;

    public ProblemHintController(ProblemHintService problemHintService) {
        this.problemHintService = problemHintService;
    }

    @GetMapping("/api/v1/problems/{problemId}/hints")
    public ResponseEntity<List<ProblemHintResponse>> findHints(@PathVariable Long problemId) {
        return ResponseEntity.ok(problemHintService.findHints(problemId));
    }
}
