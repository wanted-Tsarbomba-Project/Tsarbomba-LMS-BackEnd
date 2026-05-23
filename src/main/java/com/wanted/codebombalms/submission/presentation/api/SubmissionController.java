package com.wanted.codebombalms.submission.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionListQueryUseCase;
import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionResultQueryUseCase;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase;
import com.wanted.codebombalms.submission.presentation.api.request.SubmissionRequest;
import com.wanted.codebombalms.submission.presentation.api.response.CodeSubmissionListResponse;
import com.wanted.codebombalms.submission.presentation.api.response.CodeSubmissionResultResponse;
import com.wanted.codebombalms.submission.presentation.api.response.SubmissionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubmissionController {

    private final SubmissionCommandUseCase submissionCommandUseCase;
    private final CodeSubmissionResultQueryUseCase codeSubmissionResultQueryUseCase;
    private final CodeSubmissionListQueryUseCase codeSubmissionListQueryUseCase;

    public SubmissionController(
            SubmissionCommandUseCase submissionCommandUseCase,
            CodeSubmissionResultQueryUseCase codeSubmissionResultQueryUseCase,
            CodeSubmissionListQueryUseCase codeSubmissionListQueryUseCase
    ) {
        this.submissionCommandUseCase = submissionCommandUseCase;
        this.codeSubmissionResultQueryUseCase = codeSubmissionResultQueryUseCase;
        this.codeSubmissionListQueryUseCase = codeSubmissionListQueryUseCase;
    }

    @PostMapping("/api/v1/problems/{problemId}/submissions")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitCode(
            @PathVariable Long problemId,
            @RequestBody SubmissionRequest request
    ) {
        var command = new SubmitCodeCommand(
                request.getUserId(),
                request.getCode()
        );
        var response = new SubmissionResponse(
                submissionCommandUseCase.handle(problemId, command)
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }

    @GetMapping("/api/v1/code-submissions/{submissionId}")
    public ResponseEntity<ApiResponse<CodeSubmissionResultResponse>> getCodeSubmissionResult(
            @PathVariable Long submissionId
    ) {
        var response = new CodeSubmissionResultResponse(
                codeSubmissionResultQueryUseCase.handle(submissionId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "코드 채점 결과 조회 성공",
                response
        ));
    }

    @GetMapping("/api/v1/code-problems/{problemId}/submissions")
    public ResponseEntity<ApiResponse<CodeSubmissionListResponse>> getCodeSubmissions(
            @PathVariable Long problemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var response = new CodeSubmissionListResponse(
                codeSubmissionListQueryUseCase.handle(problemId, page, size)
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "코드 제출 기록 조회 성공",
                response
        ));
    }
}
