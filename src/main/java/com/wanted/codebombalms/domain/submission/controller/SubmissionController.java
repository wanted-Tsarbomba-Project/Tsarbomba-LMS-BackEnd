package com.wanted.codebombalms.domain.submission.controller;

import com.wanted.codebombalms.domain.submission.dto.request.SubmissionRequest;
import com.wanted.codebombalms.domain.submission.dto.response.SubmissionResponse;
import com.wanted.codebombalms.domain.submission.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubmissionController {
    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/api/v1/problems/{problemId}/submissions")
    public ResponseEntity<SubmissionResponse> submitAnswer(
            @PathVariable Long problemId,
            @RequestBody SubmissionRequest request
    ) {
        SubmissionResponse response = submissionService.submitAnswer(problemId, request);

        return ResponseEntity.ok(response);
    }
}
