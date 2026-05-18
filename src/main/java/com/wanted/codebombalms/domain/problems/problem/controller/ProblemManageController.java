package com.wanted.codebombalms.domain.problems.problem.controller;

import com.wanted.codebombalms.domain.problems.set.dto.request.ProblemSetCreateRequest;
import com.wanted.codebombalms.domain.problems.set.dto.request.ProblemSetUpdateRequest;
import com.wanted.codebombalms.domain.problems.set.dto.response.ProblemSetCreateResponse;
import com.wanted.codebombalms.domain.problems.set.dto.response.ProblemSetUpdateResponse;
import com.wanted.codebombalms.domain.problems.set.service.ProblemSetRegistrationService;
import com.wanted.codebombalms.domain.problems.set.service.ProblemSetUpdateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProblemManageController {

    private final ProblemSetRegistrationService problemSetRegistrationService;
    private final ProblemSetUpdateService  problemSetUpdateService;
    public ProblemManageController(ProblemSetRegistrationService problemSetRegistrationService,
                                   ProblemSetUpdateService problemSetUpdateService) {
        this.problemSetRegistrationService = problemSetRegistrationService;
        this.problemSetUpdateService = problemSetUpdateService;
    }

    @PostMapping("/api/v1/problems")
    public ResponseEntity<ProblemSetCreateResponse> createProblem(
            @RequestBody ProblemSetCreateRequest request
    ) {
        ProblemSetCreateResponse response =
                problemSetRegistrationService.createProblemSet(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/v1/problems/{problemSetId}")
    public ResponseEntity<ProblemSetUpdateResponse> updateProblemSet(
            @PathVariable Long problemSetId,
            @RequestBody ProblemSetUpdateRequest request
    ) {
        ProblemSetUpdateResponse response =
                problemSetUpdateService.updateProblemSet(problemSetId, request);

        return ResponseEntity.ok(response);
    }

}
