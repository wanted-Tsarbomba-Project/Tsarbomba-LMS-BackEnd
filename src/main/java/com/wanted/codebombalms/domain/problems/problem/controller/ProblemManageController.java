package com.wanted.codebombalms.domain.problems.problem.controller;

import com.wanted.codebombalms.domain.problems.set.dto.request.ProblemSetCreateRequest;
import com.wanted.codebombalms.domain.problems.set.dto.request.ProblemSetUpdateRequest;
import com.wanted.codebombalms.domain.problems.set.dto.response.ProblemSetCreateResponse;
import com.wanted.codebombalms.domain.problems.set.dto.response.ProblemSetDeleteResponse;
import com.wanted.codebombalms.domain.problems.set.dto.response.ProblemSetUpdateResponse;
import com.wanted.codebombalms.domain.problems.set.service.ProblemSetDeleteService;
import com.wanted.codebombalms.domain.problems.set.service.ProblemSetRegistrationService;
import com.wanted.codebombalms.domain.problems.set.service.ProblemSetUpdateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProblemManageController {

    private final ProblemSetRegistrationService problemSetRegistrationService;
    private final ProblemSetUpdateService  problemSetUpdateService;
    private final ProblemSetDeleteService problemSetDeleteService;

    public ProblemManageController(ProblemSetRegistrationService problemSetRegistrationService,
                                   ProblemSetUpdateService problemSetUpdateService,
                                   ProblemSetDeleteService problemSetDeleteService) {
        this.problemSetRegistrationService = problemSetRegistrationService;
        this.problemSetUpdateService = problemSetUpdateService;
        this.problemSetDeleteService = problemSetDeleteService;
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

    @DeleteMapping("/api/v1/problems/{problemSetId}")
    public ResponseEntity<ProblemSetDeleteResponse> deleteProblemSet(
            @PathVariable Long problemSetId,
            @RequestParam(defaultValue = "false") boolean force
    ) {
        ProblemSetDeleteResponse response =
                problemSetDeleteService.deactivateProblemSet(problemSetId, force);

        return ResponseEntity.ok(response);
    }
}
