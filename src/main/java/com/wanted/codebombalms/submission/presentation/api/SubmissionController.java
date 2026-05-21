package com.wanted.codebombalms.submission.presentation.api;

import com.wanted.codebombalms.submission.application.command.SubmitAnswerCommand;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase;
import com.wanted.codebombalms.submission.presentation.api.request.SubmissionRequest;
import com.wanted.codebombalms.submission.presentation.api.response.SubmissionResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubmissionController {

    private final SubmissionCommandUseCase submissionCommandUseCase;

    public SubmissionController(SubmissionCommandUseCase submissionCommandUseCase) {
        this.submissionCommandUseCase = submissionCommandUseCase;
    }

    @PostMapping("/api/v1/problems/{problemId}/submissions")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitAnswer(
            @PathVariable Long problemId,
            @RequestBody SubmissionRequest request
    ) {
        var command = new SubmitAnswerCommand(
                request.getUserId(),
                request.getSubmittedAnswer()
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
}
