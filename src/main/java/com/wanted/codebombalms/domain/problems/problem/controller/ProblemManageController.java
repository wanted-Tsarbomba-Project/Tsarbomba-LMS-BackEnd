package com.wanted.codebombalms.domain.problems.problem.controller;

import com.wanted.codebombalms.domain.problems.set.dto.request.ProblemSetCreateRequest;
import com.wanted.codebombalms.domain.problems.set.dto.response.ProblemSetCreateResponse;
import com.wanted.codebombalms.domain.problems.set.service.ProblemSetRegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProblemManageController {

    private final ProblemSetRegistrationService problemSetRegistrationService;

    public ProblemManageController(ProblemSetRegistrationService problemSetRegistrationService) {
        this.problemSetRegistrationService = problemSetRegistrationService;
    }

    @PostMapping("/api/v1/problems")
    public ResponseEntity<ProblemSetCreateResponse> createProblem(
            @RequestBody ProblemSetCreateRequest request
    ) {
        ProblemSetCreateResponse response =
                problemSetRegistrationService.createProblemSet(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
