package com.wanted.codebombalms.problems.testcase.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.testcase.application.command.CreateProblemTestCaseCommand;
import com.wanted.codebombalms.problems.testcase.application.command.UpdateProblemTestCaseCommand;
import com.wanted.codebombalms.problems.testcase.application.query.GetProblemTestCasesQuery;
import com.wanted.codebombalms.problems.testcase.application.usecase.ProblemTestCaseCommandUseCase;
import com.wanted.codebombalms.problems.testcase.application.usecase.ProblemTestCaseQueryUseCase;
import com.wanted.codebombalms.problems.testcase.presentation.api.request.CreateProblemTestCaseRequest;
import com.wanted.codebombalms.problems.testcase.presentation.api.request.UpdateProblemTestCaseRequest;
import com.wanted.codebombalms.problems.testcase.presentation.api.response.ProblemTestCaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProblemTestCaseController {

    private final ProblemTestCaseCommandUseCase commandUseCase;
    private final ProblemTestCaseQueryUseCase queryUseCase;

    public ProblemTestCaseController(
            ProblemTestCaseCommandUseCase commandUseCase,
            ProblemTestCaseQueryUseCase queryUseCase
    ) {
        this.commandUseCase = commandUseCase;
        this.queryUseCase = queryUseCase;
    }

    @PostMapping("/api/v1/problems/{problemId}/test-cases")
    public ResponseEntity<ApiResponse<ProblemTestCaseResponse>> createTestCase(
            @PathVariable Long problemId,
            @RequestBody CreateProblemTestCaseRequest request
    ) {
        var view = commandUseCase.handle(new CreateProblemTestCaseCommand(
                problemId,
                request.testCode(),
                request.expectedResult(),
                request.testOrder(),
                request.score(),
                request.isHidden(),
                request.timeoutMs()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.CREATED,
                        ApiResponseMessage.CREATED,
                        ProblemTestCaseResponse.from(view)
                ));
    }

    @GetMapping("/api/v1/problems/{problemId}/test-cases")
    public ResponseEntity<ApiResponse<List<ProblemTestCaseResponse>>> findTestCases(
            @PathVariable Long problemId
    ) {
        List<ProblemTestCaseResponse> responses = queryUseCase.handle(new GetProblemTestCasesQuery(problemId))
                .stream()
                .map(ProblemTestCaseResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                responses
        ));
    }

    @PutMapping("/api/v1/test-cases/{testCaseId}")
    public ResponseEntity<ApiResponse<ProblemTestCaseResponse>> updateTestCase(
            @PathVariable Long testCaseId,
            @RequestBody UpdateProblemTestCaseRequest request
    ) {
        var view = commandUseCase.handle(new UpdateProblemTestCaseCommand(
                testCaseId,
                request.testCode(),
                request.expectedResult(),
                request.testOrder(),
                request.score(),
                request.isHidden(),
                request.timeoutMs()
        ));

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                ProblemTestCaseResponse.from(view)
        ));
    }

    @DeleteMapping("/api/v1/test-cases/{testCaseId}")
    public ResponseEntity<ApiResponse<ProblemTestCaseResponse>> deleteTestCase(
            @PathVariable Long testCaseId
    ) {
        var view = commandUseCase.delete(testCaseId);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                ProblemTestCaseResponse.from(view)
        ));
    }
}
